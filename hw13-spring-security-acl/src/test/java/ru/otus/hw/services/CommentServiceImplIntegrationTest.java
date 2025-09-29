package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Интеграционные тесты CommentService")
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    private Book testBook1;
    private Book testBook2;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();

        Author author = mongoTemplate.save(new Author(null, "Test Author"));
        Genre genre = mongoTemplate.save(new Genre(null, "Test Genre"));
        testBook1 = mongoTemplate.save(new Book(null, "Test Book 1", author, genre));
        testBook2 = mongoTemplate.save(new Book(null, "Test Book 2", author, genre));

        testUser1 = mongoTemplate.save(new User(null, "testuser1", "password", "USER"));
        testUser2 = mongoTemplate.save(new User(null, "testuser2", "password", "USER"));
    }

    @Test
    @DisplayName("должен находить комментарий по ID")
    void shouldFindCommentById() {
        Comment commentToSave = new Comment();
        commentToSave.setText("Test Comment");
        commentToSave.setBook(testBook1);
        Comment savedComment = mongoTemplate.save(commentToSave);

        Comment foundComment = commentService.findById(savedComment.getId()).orElse(null);

        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getId()).isEqualTo(savedComment.getId());
        assertThat(foundComment.getText()).isEqualTo("Test Comment");
        assertThat(foundComment.getBook()).isNotNull();
        assertThat(foundComment.getBook().getId()).isEqualTo(testBook1.getId());
    }

    @Test
    @DisplayName("должен возвращать пустой Optional, если комментарий не найден")
    void shouldReturnEmptyIfCommentNotFound() {
        var result = commentService.findById("non-existent-id");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("должен находить комментарии по ID книги")
    void shouldFindCommentsByBookId() {
        Comment comment1 = new Comment();
        comment1.setText("Comment 1 for Book1");
        comment1.setBook(testBook1);
        comment1 = mongoTemplate.save(comment1);

        Comment comment2 = new Comment();
        comment2.setText("Comment 2 for Book1");
        comment2.setBook(testBook1);
        comment2 = mongoTemplate.save(comment2);

        Comment commentForBook2 = new Comment();
        commentForBook2.setText("Comment for Book2");
        commentForBook2.setBook(testBook2);
        mongoTemplate.save(commentForBook2);

        List<Comment> commentsForBook1 = commentService.findByBookId(testBook1.getId());

        assertThat(commentsForBook1).hasSize(2);
        assertThat(commentsForBook1).extracting(Comment::getId)
                .containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
        assertThat(commentsForBook1).allSatisfy(comment -> {
            assertThat(comment.getBook()).isNotNull();
            assertThat(comment.getBook().getId()).isEqualTo(testBook1.getId());
        });
    }

    @Test
    @DisplayName("должен создавать новый комментарий")
    void shouldCreateNewComment() {
        Comment createdComment = commentService.create("New Comment", testBook1.getId(), testUser1);
        assertThat(createdComment.getId()).isNotNull();
        assertThat(createdComment.getText()).isEqualTo("New Comment");
        assertThat(createdComment.getUser().getId()).isEqualTo(testUser1.getId());
        Comment foundComment = mongoTemplate.findById(createdComment.getId(), Comment.class);
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getText()).isEqualTo("New Comment");
        assertThat(foundComment.getBook().getId()).isEqualTo(testBook1.getId());
        assertThat(foundComment.getUser().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("должен обновлять существующий комментарий")
    void shouldUpdateExistingComment() {
        Comment commentToSave = new Comment();
        commentToSave.setText("Original Text");
        commentToSave.setBook(testBook1);
        commentToSave.setUser(testUser1);
        Comment savedComment = mongoTemplate.save(commentToSave);

        Comment updatedComment = commentService.update(savedComment.getId(), "Updated Text", testUser1);
        assertThat(updatedComment.getId()).isEqualTo(savedComment.getId());
        assertThat(updatedComment.getText()).isEqualTo("Updated Text");
        Comment foundComment = mongoTemplate.findById(savedComment.getId(), Comment.class);
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getText()).isEqualTo("Updated Text");
    }

    @Test
    @DisplayName("должен обновлять существующий комментарий только владельцем")
    void shouldUpdateExistingCommentOnlyByOwner() {
        Comment commentToSave = new Comment();
        commentToSave.setText("Original Text");
        commentToSave.setBook(testBook1);
        commentToSave.setUser(testUser1);
        Comment savedComment = mongoTemplate.save(commentToSave);

        assertThatThrownBy(() -> commentService.update(savedComment.getId(), "Hacker Text", testUser2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found or not owned by user");

        Comment foundComment = mongoTemplate.findById(savedComment.getId(), Comment.class);
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getText()).isEqualTo("Original Text");
    }

    @Test
    @DisplayName("должен удалять комментарий по ID")
    void shouldDeleteCommentById() {
        Comment commentToSave = new Comment();
        commentToSave.setText("Comment to Delete");
        commentToSave.setBook(testBook1);
        commentToSave.setUser(testUser1);
        Comment savedComment = mongoTemplate.save(commentToSave);

        commentService.deleteById(savedComment.getId(), testUser1);
        assertThat(mongoTemplate.findById(savedComment.getId(), Comment.class)).isNull();
    }

    @Test
    @DisplayName("должен удалять комментарий только владельцем")
    void shouldDeleteCommentOnlyByOwner() {
        Comment commentToSave = new Comment();
        commentToSave.setText("Comment to Delete");
        commentToSave.setBook(testBook1);
        commentToSave.setUser(testUser1);
        Comment savedComment = mongoTemplate.save(commentToSave);

        assertThatThrownBy(() -> commentService.deleteById(savedComment.getId(), testUser2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found or not owned by user");

        assertThat(mongoTemplate.findById(savedComment.getId(), Comment.class)).isNotNull();
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке обновить несуществующий комментарий")
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistentComment() {
        assertThatThrownBy(() -> commentService.update("non-existent-id", "New Text", testUser1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id non-existent-id not found");
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке создать комментарий для несуществующей книги")
    void shouldThrowEntityNotFoundExceptionWhenCreatingCommentForNonExistentBook() {
        assertThatThrownBy(() -> commentService.create("Text", "non-existent-book-id", testUser1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id non-existent-book-id not found");
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке удалить несуществующий комментарий")
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistentComment() {
        assertThatThrownBy(() -> commentService.deleteById("non-existent-id", testUser1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id non-existent-id not found");
    }
}