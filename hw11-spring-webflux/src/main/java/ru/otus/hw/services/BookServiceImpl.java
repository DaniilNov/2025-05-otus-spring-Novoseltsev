package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    @Override
    public Mono<Book> findById(String id) {
        return bookRepository.findBookById(id);
    }

    @Override
    public Flux<Book> findAll() {
        return bookRepository.findAllBooks();
    }

    @Override
    public Mono<Book> insert(String title, String authorId, String genreId) {
        return createBook(null, title, authorId, genreId);
    }

    @Override
    public Mono<Book> update(String id, String title, String authorId, String genreId) {
        return createBook(id, title, authorId, genreId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return commentRepository.deleteByBookId(id)
                .then(bookRepository.deleteById(id));
    }

    private Mono<Book> createBook(String id, String title, String authorId, String genreId) {
        Mono<Author> authorMono = authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %s not found".formatted(authorId))));

        Mono<Genre> genreMono = genreRepository.findById(genreId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Genre with id %s not found".formatted(genreId))));

        return Mono.zip(authorMono, genreMono)
                .flatMap(tuple -> {
                    Author author = tuple.getT1();
                    Genre genre = tuple.getT2();

                    Book book = new Book(id, title, author, genre);
                    return bookRepository.saveBook(book);
                });
    }
}