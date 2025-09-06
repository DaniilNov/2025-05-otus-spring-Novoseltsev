package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Author;

@SpringBootTest
@DisplayName("Интеграционные тесты AuthorService")
class AuthorServiceImplIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        reactiveMongoTemplate.getCollection("authors")
                .flatMap(collection -> Mono.from(collection.drop()))
                .subscribe();
    }

    @Test
    @DisplayName("должен находить всех авторов")
    void shouldFindAllAuthors() {
        Author author1 = new Author(null, "Author 1");
        Author author2 = new Author(null, "Author 2");

        StepVerifier.create(
                        reactiveMongoTemplate.save(author1)
                                .then(reactiveMongoTemplate.save(author2))
                                .thenMany(authorService.findAll())
                )
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(authorService.findAll())
                .expectNextMatches(author -> "Author 1".equals(author.getFullName()))
                .expectNextMatches(author -> "Author 2".equals(author.getFullName()))
                .verifyComplete();
    }
}