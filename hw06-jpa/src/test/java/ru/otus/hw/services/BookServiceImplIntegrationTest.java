package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Сервис для работы с книгами")
@ActiveProfiles("test")
@SpringBootTest
class BookServiceImplIntegrationTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("при загрузке книги должен корректно загружать автора без LazyInitializationException")
    @Transactional
    void shouldLoadAuthorWithoutLazyException() {
        Book book = bookService.findById(1L).orElseThrow();

        assertThatNoException().isThrownBy(() -> {
            String authorName = book.getAuthor().getFullName();
            assertThat(authorName).isEqualTo("Author_1");
        });
    }

    @Test
    @DisplayName("при загрузке книги должен корректно загружать жанр без LazyInitializationException")
    @Transactional
    void shouldLoadGenreWithoutLazyException() {
        Book book = bookService.findById(1L).orElseThrow();

        assertThatNoException().isThrownBy(() -> {
            String genreName = book.getGenre().getName();
            assertThat(genreName).isEqualTo("Genre_1");
        });
    }

    @Test
    @DisplayName("при загрузке книги должен корректно загружать комментарии без LazyInitializationException")
    @Transactional
    void shouldLoadCommentsWithoutLazyException() {
        Book book = bookService.findById(1L).orElseThrow();

        assertThatNoException().isThrownBy(() -> {
            assertThat(book.getComments())
                    .hasSize(1)
                    .extracting(Comment::getText)
                    .containsExactly("Great book!");
        });
    }


}