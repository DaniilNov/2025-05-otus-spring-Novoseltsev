package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Репозиторий на основе JPA для работы с жанрами")
@DataJpaTest
class JpaGenreRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GenreRepository repository;

    @Test
    @DisplayName("должен загружать список всех жанров")
    void shouldFindAllGenres() {
        em.getEntityManager().createQuery("DELETE FROM Genre").executeUpdate();
        em.flush();

        Genre genre1 = em.persist(new Genre(0, "Genre 1"));
        Genre genre2 = em.persist(new Genre(0, "Genre 2"));
        em.flush();
        em.clear();

        List<Genre> genres = repository.findAll();

        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre 1", "Genre 2");
    }

    @Test
    @DisplayName("должен загружать жанр по id")
    void shouldFindGenreById() {
        Genre expected = em.persist(new Genre(0, "Test Genre"));
        em.flush();

        Optional<Genre> actual = repository.findById(expected.getId());

        assertThat(actual)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("должен возвращать пустой Optional, если жанр не найден")
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> actual = repository.findById(999L);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("не должен бросать исключение при работе с пустой таблицей")
    void shouldNotThrowWithEmptyTable() {
        em.getEntityManager().createQuery("DELETE FROM Genre").executeUpdate();
        em.flush();

        assertThatNoException()
                .isThrownBy(() -> {
                    List<Genre> genres = repository.findAll();
                    Optional<Genre> genre = repository.findById(1L);
                });
        assertThat(repository.findAll()).isEmpty();
    }
}