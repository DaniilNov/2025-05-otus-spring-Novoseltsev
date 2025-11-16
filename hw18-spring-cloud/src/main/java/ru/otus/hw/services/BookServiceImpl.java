package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "findByIdFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public Optional<Book> findById(String id) {
        log.debug("Finding book by id: {}", id);
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "findAllFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public List<Book> findAll() {
        log.debug("Finding all books");
        return bookRepository.findAll();
    }

    @Transactional
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "insertFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public Book insert(String title, String authorId, String genreId) {
        log.debug("Inserting book: title={}, authorId={}, genreId={}", title, authorId, genreId);
        return save(null, title, authorId, genreId);
    }

    @Transactional
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "updateFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public Book update(String id, String title, String authorId, String genreId) {
        log.debug("Updating book: id={}, title={}, authorId={}, genreId={}", id, title, authorId, genreId);
        return save(id, title, authorId, genreId);
    }

    @Transactional
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "deleteFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public void deleteById(String id) {
        log.debug("Deleting book by id: {}", id);
        commentRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
    }


    public Optional<Book> findByIdFallback(String id, Throwable t) {
        log.error("Fallback for findById({}): {}", id, t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Book lookup is temporarily unavailable. Please try again later.",
                t
        );
    }

    public List<Book> findAllFallback(Throwable t) {
        log.error("Fallback for findAll(): {}", t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Book list is temporarily unavailable. Please try again later.",
                t
        );
    }

    public Book insertFallback(String title, String authorId, String genreId, Throwable t) {
        log.error("Fallback for insert({},{},{}): {}", title, authorId, genreId, t.toString());
        EntityNotFoundException enf = findEntityNotFound(t);
        if (enf != null) {
            throw enf;
        }
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Saving book is temporarily unavailable. Please try again later.",
                t
        );
    }

    public Book updateFallback(String id, String title, String authorId, String genreId, Throwable t) {
        log.error("Fallback for update({},{},{},{}): {}", id, title, authorId, genreId, t.toString());
        EntityNotFoundException enf = findEntityNotFound(t);
        if (enf != null) {
            throw enf;
        }
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Updating book is temporarily unavailable. Please try again later.",
                t
        );
    }

    public void deleteFallback(String id, Throwable t) {
        log.error("Fallback for deleteById({}): {}", id, t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Deleting book is temporarily unavailable. Please try again later.",
                t
        );
    }

    private Book save(String id, String title, String authorId, String genreId) {
        Author author = findAuthorById(authorId);
        Genre genre = findGenreById(genreId);
        Book book = createBook(id, title, author, genre);
        return bookRepository.save(book);
    }

    private Author findAuthorById(String authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(authorId)));
    }

    private Genre findGenreById(String genreId) {
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %s not found".formatted(genreId)));
    }

    private Book createBook(String id, String title, Author author, Genre genre) {
        Book book = new Book();
        if (id != null && !id.isEmpty()) {
            book.setId(id);
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        return book;
    }

    private static EntityNotFoundException findEntityNotFound(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof EntityNotFoundException enf) {
                return enf;
            }
            Throwable next = cur.getCause();
            if (next == cur) {
                break;
            }
            cur = next;
        }
        return null;
    }
}