package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с авторами")
@DataJpaTest
@Import(JpaAuthorRepository.class)
class JpaAuthorRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JpaAuthorRepository repository;

    @Test
    @DisplayName("должен загружать всех авторов")
    void shouldFindAllAuthors() {

        EntityManager entityManager = em.getEntityManager();

        entityManager.createQuery("DELETE FROM Author").executeUpdate();
        em.flush();

        Author author1 = em.persist(new Author(0, "Author 1"));
        Author author2 = em.persist(new Author(0, "Author 2"));
        em.flush();
        em.clear();

        List<Author> authors = repository.findAll();

        assertThat(authors)
                .hasSize(2)
                .extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Author 1", "Author 2");
    }

    @Test
    @DisplayName("должен загружать автора по id")
    void shouldFindAuthorById() {
        Author expected = em.persist(new Author(0, "Test Author"));
        em.flush();

        Optional<Author> actual = repository.findById(expected.getId());

        assertThat(actual)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("должен возвращать пустой Optional, если автор не найден")
    void shouldReturnEmptyWhenAuthorNotFound() {
        Optional<Author> actual = repository.findById(333L);
        assertThat(actual).isEmpty();
    }
}