package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

@SpringBootTest
@DisplayName("Интеграционные тесты CommentService")
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private String bookId1;
    private String bookId2;

    @BeforeEach
    void setUp() {
        reactiveMongoTemplate.getCollectionNames()
                .flatMap(collectionName ->
                        reactiveMongoTemplate.getCollection(collectionName)
                                .flatMap(collection -> Mono.from(collection.drop()))
                )
                .then()
                .block();

        Author author = new Author(null, "Test Author");
        Genre genre = new Genre(null, "Test Genre");

        author = reactiveMongoTemplate.save(author).block();
        genre = reactiveMongoTemplate.save(genre).block();

        String authorId = author.getId();
        String genreId = genre.getId();

        Book book1 = new Book(null, "Test Book 1", authorId, genreId);
        Book book2 = new Book(null, "Test Book 2", authorId, genreId);

        book1 = reactiveMongoTemplate.save(book1).block();
        book2 = reactiveMongoTemplate.save(book2).block();

        this.bookId1 = book1.getId();
        this.bookId2 = book2.getId();
    }

    @Test
    @DisplayName("должен находить комментарий по ID")
    void shouldFindCommentById() {
        Comment commentToSave = new Comment(null, "Test Comment", bookId1);
        String commentId = reactiveMongoTemplate.save(commentToSave).block().getId();

        StepVerifier.create(commentService.findDtoById(commentId))
                .expectNextMatches(commentDto ->
                        "Test Comment".equals(commentDto.getText()) &&
                                "Test Book 1".equals(commentDto.getBook().getTitle())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен находить комментарии по ID книги")
    void shouldFindCommentsByBookId() {
        Comment comment1 = new Comment(null, "Comment 1 for Book1", bookId1);
        Comment comment2 = new Comment(null, "Comment 2 for Book1", bookId1);
        Comment comment3 = new Comment(null, "Comment for Book2", bookId2);

        reactiveMongoTemplate.save(comment1).block();
        reactiveMongoTemplate.save(comment2).block();
        reactiveMongoTemplate.save(comment3).block();

        StepVerifier.create(commentService.findDtosByBookId(bookId1))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("должен создавать новый комментарий")
    void shouldCreateNewComment() {
        StepVerifier.create(commentService.create("New Comment", bookId1))
                .expectNextMatches(comment ->
                        "New Comment".equals(comment.getText()) &&
                                bookId1.equals(comment.getBookId()) &&
                                comment.getId() != null
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен обновлять существующий комментарий")
    void shouldUpdateExistingComment() {
        Comment commentToSave = new Comment(null, "Original Text", bookId1);
        String commentId = reactiveMongoTemplate.save(commentToSave).block().getId();

        StepVerifier.create(commentService.update(commentId, "Updated Text"))
                .expectNextMatches(comment ->
                        "Updated Text".equals(comment.getText()) &&
                                commentId.equals(comment.getId())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен удалять комментарий по ID")
    void shouldDeleteCommentById() {
        Comment commentToSave = new Comment(null, "Comment to Delete", bookId1);
        String commentId = reactiveMongoTemplate.save(commentToSave).block().getId();

        StepVerifier.create(commentService.deleteById(commentId))
                .verifyComplete();

        StepVerifier.create(reactiveMongoTemplate.findById(commentId, Comment.class))
                .verifyComplete();
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке обновить несуществующий комментарий")
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistentComment() {
        StepVerifier.create(commentService.update("non-existent-id", "New Text"))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке создать комментарий для несуществующей книги")
    void shouldThrowEntityNotFoundExceptionWhenCreatingCommentForNonExistentBook() {
        StepVerifier.create(commentService.create("Text", "non-existent-book-id"))
                .expectError(EntityNotFoundException.class)
                .verify();
    }
}