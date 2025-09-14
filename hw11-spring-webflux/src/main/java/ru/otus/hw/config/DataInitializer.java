package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.projections.BookProjection;
import ru.otus.hw.projections.CommentProjection;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookProjectionRepository;
import ru.otus.hw.repositories.CommentProjectionRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookProjectionRepository bookProjectionRepository;

    private final CommentProjectionRepository commentProjectionRepository;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        reactiveMongoTemplate.getCollection("authors")
                .flatMap(collection ->
                        Mono.from(collection.estimatedDocumentCount())
                                .map(count -> count)
                )
                .doOnNext(count -> {
                    if (count == 0) {
                        log.info("Initializing sample data...");
                        initializeData().subscribe();
                        log.info("Sample data successfully loaded.");
                    } else {
                        log.info("Data already exists, skipping initialization.");
                    }
                })
                .doOnError(ex -> log.error("Error during data initialization", ex))
                .subscribe();

        log.info("Data initialization finished.");
    }

    private Mono<Void> initializeData() {
        Mono<Void> dropStep = reactiveMongoTemplate.getCollectionNames()
                .flatMap(collectionName ->
                        reactiveMongoTemplate.getCollection(collectionName)
                                .flatMap(collection -> Mono.from(collection.drop()))
                )
                .then();

        Mono<List<Author>> saveAuthorsStep = dropStep.thenMany(createAndSaveAuthors()).collectList();

        Mono<List<Genre>> saveGenresStep = dropStep.thenMany(createAndSaveGenres()).collectList();

        Mono<List<BookProjection>> saveBooksStep = Mono.zip(saveAuthorsStep, saveGenresStep)
                .flatMap(tuple -> {
                    List<Author> authors = tuple.getT1();
                    List<Genre> genres = tuple.getT2();
                    return createAndSaveBooks(authors, genres).collectList();
                });

        return saveBooksStep
                .flatMap(books -> createAndSaveComments(books).then());
    }

    private Flux<Author> createAndSaveAuthors() {
        Author author1 = new Author(null, "Author_1");
        Author author2 = new Author(null, "Author_2");
        Author author3 = new Author(null, "Author_3");
        return authorRepository.saveAll(List.of(author1, author2, author3))
                .doOnNext(author -> log.debug("Saved author: {}", author.getFullName()));
    }

    private Flux<Genre> createAndSaveGenres() {
        Genre genre1 = new Genre(null, "Genre_1");
        Genre genre2 = new Genre(null, "Genre_2");
        Genre genre3 = new Genre(null, "Genre_3");
        return genreRepository.saveAll(List.of(genre1, genre2, genre3))
                .doOnNext(genre -> log.debug("Saved genre: {}", genre.getName()));
    }

    private Flux<BookProjection> createAndSaveBooks(List<Author> savedAuthors, List<Genre> savedGenres) {
        BookProjection book1 = new BookProjection(null, "BookTitle_1",
                savedAuthors.get(0).getId(), savedGenres.get(0).getId());
        BookProjection book2 = new BookProjection(null, "BookTitle_2",
                savedAuthors.get(1).getId(), savedGenres.get(1).getId());
        BookProjection book3 = new BookProjection(null, "BookTitle_3",
                savedAuthors.get(2).getId(), savedGenres.get(2).getId());

        return bookProjectionRepository.saveAll(List.of(book1, book2, book3))
                .doOnNext(book -> log.debug("Saved book: {}", book.getTitle()));
    }

    private Mono<Void> createAndSaveComments(List<BookProjection> savedBooks) {
        CommentProjection comment1 = new CommentProjection(null, "Great book!", savedBooks.get(0).getId());
        CommentProjection comment2 = new CommentProjection(null, "Could be better", savedBooks.get(1).getId());
        CommentProjection comment3 = new CommentProjection(null, "Not bad", savedBooks.get(2).getId());

        return commentProjectionRepository.saveAll(List.of(comment1, comment2, comment3))
                .doOnNext(comment -> log.debug("Saved comment: {}", comment.getText()))
                .then();
    }
}