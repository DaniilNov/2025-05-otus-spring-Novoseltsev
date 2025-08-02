package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Репозиторий на основе JPA для работы с комментариями")
@DataJpaTest
@Import(JpaCommentRepository.class)
class JpaCommentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JpaCommentRepository repository;

    @Test
    @DisplayName("должен находить комментарий по id")
    void shouldFindCommentById() {
        Book book = em.persist(new Book(0, "Book Title",
                em.persist(new Author(0, "Author")),
                em.persist(new Genre(0, "Genre"))));
        Comment expected = em.persist(new Comment(0, "Test Comment", book));
        em.flush();
        em.clear();

        Optional<Comment> actual = repository.findById(expected.getId());

        assertThat(actual)
                .isPresent()
                .get()
                .extracting(Comment::getText, c -> c.getBook().getId())
                .containsExactly("Test Comment", book.getId());
    }

    @Test
    @DisplayName("должен возвращать пустой Optional, если комментарий не найден")
    void shouldReturnEmptyWhenCommentNotFound() {
        Optional<Comment> actual = repository.findById(999L);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("должен находить все комментарии по id книги")
    void shouldFindCommentsByBookId() {
        Book book1 = em.persist(new Book(0, "Book 1",
                em.persist(new Author(0, "Author 1")),
                em.persist(new Genre(0, "Genre 1"))));
        Book book2 = em.persist(new Book(0, "Book 2",
                em.persist(new Author(0, "Author 2")),
                em.persist(new Genre(0, "Genre 2"))));

        Comment comment1 = em.persist(new Comment(0, "Comment 1", book1));
        Comment comment2 = em.persist(new Comment(0, "Comment 2", book1));
        em.persist(new Comment(0, "Comment 3", book2));
        em.flush();
        em.clear();

        List<Comment> comments = repository.findByBookId(book1.getId());

        assertThat(comments)
                .hasSize(2)
                .extracting(Comment::getText, c -> c.getBook().getId())
                .containsExactlyInAnyOrder(
                        tuple("Comment 1", book1.getId()),
                        tuple("Comment 2", book1.getId()));
    }

    @Test
    @DisplayName("должен сохранять новый комментарий")
    void shouldSaveNewComment() {
        Book book = em.persist(new Book(0, "Book",
                em.persist(new Author(0, "Author")),
                em.persist(new Genre(0, "Genre"))));
        em.flush();

        Comment newComment = new Comment(0, "New Comment", book);
        Comment saved = repository.save(newComment);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, saved.getId());
        assertThat(found)
                .extracting(Comment::getText, c -> c.getBook().getId())
                .containsExactly("New Comment", book.getId());
    }

    @Test
    @DisplayName("должен обновлять существующий комментарий")
    void shouldUpdateExistingComment() {
        Book book = em.persist(new Book(0, "Book",
                em.persist(new Author(0, "Author")),
                em.persist(new Genre(0, "Genre"))));
        Comment existing = em.persist(new Comment(0, "Old Comment", book));
        em.flush();

        existing.setText("Updated Comment");
        Comment updated = repository.save(existing);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, updated.getId());
        assertThat(found.getText()).isEqualTo("Updated Comment");
    }

    @Test
    @DisplayName("должен удалять комментарий по id")
    void shouldDeleteCommentById() {
        Book book = em.persist(new Book(0, "Book",
                em.persist(new Author(0, "Author")),
                em.persist(new Genre(0, "Genre"))));
        Comment comment = em.persist(new Comment(0, "To Delete", book));
        em.flush();

        repository.deleteById(comment.getId());
        em.flush();

        assertThat(em.find(Comment.class, comment.getId())).isNull();
    }

    @Test
    @DisplayName("не должен бросать исключение при удалении несуществующего комментария")
    void shouldNotThrowWhenDeletingNonExistingComment() {
        assertThatNoException().isThrownBy(() -> repository.deleteById(333L));
    }
}