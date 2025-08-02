package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Сервис для работы с комментариями")
@ActiveProfiles("test")
@SpringBootTest
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("при загрузке комментария должен корректно загружать книгу без LazyInitializationException")
    @Transactional
    void shouldLoadBookWithoutLazyException() {
        Comment comment = commentService.findById(1L).orElseThrow();

        assertThatNoException().isThrownBy(() -> {
            String bookTitle = comment.getBook().getTitle();
            assertThat(bookTitle).isEqualTo("BookTitle_1");
        });
    }

    @Test
    @DisplayName("при загрузке комментария должен корректно загружать автора книги без LazyInitializationException")
    @Transactional
    void shouldLoadBooksAuthorWithoutLazyException() {
        Comment comment = commentService.findById(1L).orElseThrow();

        assertThatNoException().isThrownBy(() -> {
            String authorName = comment.getBook().getAuthor().getFullName();
            assertThat(authorName).isEqualTo("Author_1");
        });
    }
}