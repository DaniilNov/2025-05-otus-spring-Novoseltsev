package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ServiceTemporarilyUnavailableException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final ResilientExecutor resilientExecutor;

    @Transactional(readOnly = true)
    @Override
    public Optional<Book> findById(String id) {
        return resilientExecutor.executeOrFallback(() -> bookRepository.findById(id), Optional::empty);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Book> findAll() {
        return resilientExecutor.executeOrFallback(bookRepository::findAll, Collections::emptyList);
    }

    @Transactional
    @Override
    public Book insert(String title, String authorId, String genreId) {
        return save(null, title, authorId, genreId);
    }

    @Transactional
    @Override
    public Book update(String id, String title, String authorId, String genreId) {
        return save(id, title, authorId, genreId);
    }

    @Transactional
    @Override
    public void deleteById(String id) {
        resilientExecutor.executeVoidOrFallback(
                () -> commentRepository.deleteByBookId(id),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Deleting comments is temporarily unavailable");
                }
        );
        resilientExecutor.executeVoidOrFallback(
                () -> bookRepository.deleteById(id),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Deleting book is temporarily unavailable");
                }
        );
    }

    private Book save(String id, String title, String authorId, String genreId) {
        Author author = loadAuthorOrThrow(authorId);
        Genre genre = loadGenreOrThrow(genreId);
        Book book = createBook(id, title, author, genre);
        return saveBook(book);
    }

    private Author loadAuthorOrThrow(String authorId) {
        return resilientExecutor.executeOrFallback(
                () -> authorRepository.findById(authorId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Author with id %s not found".formatted(authorId))),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Author lookup is temporarily unavailable");
                }
        );
    }

    private Genre loadGenreOrThrow(String genreId) {
        return resilientExecutor.executeOrFallback(
                () -> genreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("Genre with id %s not found".formatted(genreId))),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Genre lookup is temporarily unavailable");
                }
        );
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

    private Book saveBook(Book book) {
        return resilientExecutor.executeOrFallback(
                () -> bookRepository.save(book),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Saving book is temporarily unavailable");
                }
        );
    }
}