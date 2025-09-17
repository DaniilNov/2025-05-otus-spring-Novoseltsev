package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Genre;

@SpringBootTest
@DisplayName("Интеграционные тесты GenreService")
class GenreServiceImplIntegrationTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        reactiveMongoTemplate.getCollection("genres")
                .flatMap(collection -> Mono.from(collection.drop()))
                .block();
    }

    @Test
    @DisplayName("должен находить все жанры")
    void shouldFindAllGenres() {
        Genre genre1 = new Genre(null, "Genre 1");
        Genre genre2 = new Genre(null, "Genre 2");

        StepVerifier.create(
                        reactiveMongoTemplate.save(genre1)
                                .then(reactiveMongoTemplate.save(genre2))
                                .thenMany(genreService.findAll())
                )
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(genreService.findAll())
                .expectNextMatches(genre -> "Genre 1".equals(genre.getName()))
                .expectNextMatches(genre -> "Genre 2".equals(genre.getName()))
                .verifyComplete();
    }
}