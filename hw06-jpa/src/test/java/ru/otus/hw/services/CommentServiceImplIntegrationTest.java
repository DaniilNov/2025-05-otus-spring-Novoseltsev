package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

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
        Optional<Comment> optionalComment = commentService.findById(1L);

        assertThat(optionalComment).isPresent();

        Comment comment = optionalComment.get();

        assertThat(comment.getText()).isEqualTo("Great book!");
        assertThat(comment.getBook()).isNotNull();
    }

    @Test
    @DisplayName("При получении комментариев по ID книги все книги должны быть доступны без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldFindCommentsByBookIdWithBooksWithoutLazyException() {
        List<Comment> comments = commentService.findByBookId(2L);

        assertThat(comments).isNotEmpty();

        comments.forEach(comment -> {
            assertThat(comment.getBook()).isNotNull();
        });
    }
}
