package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Интеграционные тесты GenreService")
class GenreServiceImplIntegrationTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();

        mongoTemplate.save(new Genre(null, "Genre 1"));
        mongoTemplate.save(new Genre(null, "Genre 2"));
    }

    @Test
    @DisplayName("должен находить все жанры")
    void shouldFindAllGenres() {
        List<Genre> genres = genreService.findAll();

        assertThat(genres).hasSize(2);
        assertThat(genres).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre 1", "Genre 2");
    }

}