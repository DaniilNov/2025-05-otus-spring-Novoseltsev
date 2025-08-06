package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Интеграционные тесты CommentService")
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("При получении комментария по ID книга должна быть доступна без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldFindCommentByIdWithBookWithoutLazyException() {
        Comment comment = commentService.findById(1L).orElseThrow();

        assertThat(comment.getText()).isEqualTo("Great book!");
        assertThat(comment.getBook())
                .isNotNull()
                .extracting(Book::getTitle)
                .isEqualTo("BookTitle_1");
    }

    @Test
    @DisplayName("При получении комментариев по ID книги все книги должны быть доступны без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldFindCommentsByBookIdWithBooksWithoutLazyException() {
        List<Comment> comments = commentService.findByBookId(2L);

        assertThat(comments)
                .hasSize(2)
                .allSatisfy(comment -> {
                    assertThat(comment.getBook()).isNotNull();
                    assertThat(comment.getBook().getId()).isEqualTo(2L);
                })
                .extracting(Comment::getText)
                .containsExactlyInAnyOrder("Could be better", "Good");
    }
}
