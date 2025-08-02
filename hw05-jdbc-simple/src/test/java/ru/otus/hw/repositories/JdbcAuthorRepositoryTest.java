package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с авторами")
@JdbcTest
@Import(JdbcAuthorRepository.class)
class JdbcAuthorRepositoryTest {

    @Autowired
    private JdbcAuthorRepository repository;

    @Test
    @DisplayName("должен загружать всех авторов")
    void shouldFindAllAuthors() {
        List<Author> authors = repository.findAll();
        assertThat(authors).hasSize(3);
        assertThat(authors.stream().map(Author::getFullName))
                .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
    }

    @Test
    @DisplayName("должен загружать автора по id")
    void shouldFindAuthorById() {
        Optional<Author> author = repository.findById(1L);
        assertThat(author).isPresent();
        assertThat(author.get().getFullName()).isEqualTo("Author_1");
    }

    @Test
    @DisplayName("должен возвращать пустой Optional для несуществующего автора")
    void shouldReturnEmptyOptionalForNonExistingAuthor() {
        Optional<Author> author = repository.findById(99L);
        assertThat(author).isEmpty();
    }
}