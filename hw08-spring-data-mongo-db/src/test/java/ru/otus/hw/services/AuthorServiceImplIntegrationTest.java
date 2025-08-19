package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Интеграционные тесты AuthorService")
class AuthorServiceImplIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();
    }

    @Test
    @DisplayName("должен находить всех авторов")
    void shouldFindAllAuthors() {
        Author author1 = authorRepository.save(new Author(null, "Author 1"));
        Author author2 = authorRepository.save(new Author(null, "Author 2"));

        List<Author> authors = authorService.findAll();

        assertThat(authors).hasSize(2);
        assertThat(authors).extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Author 1", "Author 2");
    }

}