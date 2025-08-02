package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с жанрами")
@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repositoryJdbc;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("должен загружать список всех жанров")
    void shouldReturnCorrectGenresList() {
        var actualGenres = repositoryJdbc.findAll();
        assertThat(actualGenres).hasSize(3);
        assertThat(actualGenres.stream().map(Genre::getName).toList())
                .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3");
    }

    @Test
    @DisplayName("должен загружать жанр по id")
    void shouldReturnCorrectGenreById() {
        var actualGenre = repositoryJdbc.findById(1L);
        assertThat(actualGenre).isPresent()
                .get()
                .matches(genre -> genre.getName().equals("Genre_1"));
    }

    @Test
    @DisplayName("должен возвращать пустой Optional для несуществующего жанра")
    void shouldReturnEmptyOptionalForNonExistingGenre() {
        var actualGenre = repositoryJdbc.findById(99L);
        assertThat(actualGenre).isEmpty();
    }
}