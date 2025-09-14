package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.projections.BookProjection;

@Repository
@RequiredArgsConstructor
public class BookCustomRepositoryImpl implements BookCustomRepository {

    private final BookProjectionRepository bookProjectionRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    public Mono<Book> findBookById(String id) {
        return bookProjectionRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %s not found".formatted(id))))
                .flatMap(this::convertToBook);
    }

    @Override
    public Flux<Book> findAllBooks() {
        return bookProjectionRepository.findAll()
                .flatMap(this::convertToBook);
    }

    @Override
    public Mono<Book> saveBook(Book book) {
        BookProjection projection = convertToProjection(book);
        return bookProjectionRepository.save(projection)
                .flatMap(this::convertToBook);
    }

    private Mono<Book> convertToBook(BookProjection projection) {
        Mono<Author> authorMono = authorRepository.findById(projection.getAuthorId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %s not found".formatted(projection.getAuthorId()))));

        Mono<Genre> genreMono = genreRepository.findById(projection.getGenreId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Genre with id %s not found".formatted(projection.getGenreId()))));

        return Mono.zip(authorMono, genreMono)
                .map(tuple -> new Book(
                        projection.getId(),
                        projection.getTitle(),
                        tuple.getT1(),
                        tuple.getT2()
                ));
    }

    private BookProjection convertToProjection(Book book) {
        return new BookProjection(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getGenre().getId()
        );
    }
}
