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
import ru.otus.hw.models.Genre;

@SpringBootTest
@DisplayName("Интеграционные тесты BookService")
class BookServiceImplIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private String authorId1;
    private String authorId2;
    private String genreId1;
    private String genreId2;

    @BeforeEach
    void setUp() {
        reactiveMongoTemplate.getCollectionNames()
                .flatMap(collectionName ->
                        reactiveMongoTemplate.getCollection(collectionName)
                                .flatMap(collection -> Mono.from(collection.drop()))
                )
                .then()
                .block();

        Author author1 = new Author(null, "Test Author 1");
        Author author2 = new Author(null, "Test Author 2");
        Genre genre1 = new Genre(null, "Test Genre 1");
        Genre genre2 = new Genre(null, "Test Genre 2");

        author1 = reactiveMongoTemplate.save(author1).block();
        author2 = reactiveMongoTemplate.save(author2).block();
        genre1 = reactiveMongoTemplate.save(genre1).block();
        genre2 = reactiveMongoTemplate.save(genre2).block();

        this.authorId1 = author1.getId();
        this.authorId2 = author2.getId();
        this.genreId1 = genre1.getId();
        this.genreId2 = genre2.getId();
    }

    @Test
    @DisplayName("должен находить все книги")
    void shouldFindAllBooks() {
        Book book1 = new Book(null, "Book 1", authorId1, genreId1);
        Book book2 = new Book(null, "Book 2", authorId2, genreId2);

        reactiveMongoTemplate.save(book1).block();
        reactiveMongoTemplate.save(book2).block();

        StepVerifier.create(bookService.findAll())
                .expectNextMatches(bookDto ->
                        "Book 1".equals(bookDto.getTitle()) &&
                                "Test Author 1".equals(bookDto.getAuthor().getFullName()) &&
                                "Test Genre 1".equals(bookDto.getGenre().getName())
                )
                .expectNextMatches(bookDto ->
                        "Book 2".equals(bookDto.getTitle()) &&
                                "Test Author 2".equals(bookDto.getAuthor().getFullName()) &&
                                "Test Genre 2".equals(bookDto.getGenre().getName())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен находить книгу по ID")
    void shouldFindBookById() {
        Book bookToSave = new Book(null, "Test Book", authorId1, genreId1);
        String bookId = reactiveMongoTemplate.save(bookToSave).block().getId();

        StepVerifier.create(bookService.findDtoById(bookId))
                .expectNextMatches(bookDto ->
                        "Test Book".equals(bookDto.getTitle()) &&
                                "Test Author 1".equals(bookDto.getAuthor().getFullName()) &&
                                "Test Genre 1".equals(bookDto.getGenre().getName())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен вставлять новую книгу")
    void shouldInsertNewBook() {
        StepVerifier.create(bookService.insert("New Book", authorId1, genreId1))
                .expectNextMatches(bookDto ->
                        "New Book".equals(bookDto.getTitle()) &&
                                "Test Author 1".equals(bookDto.getAuthor().getFullName()) &&
                                "Test Genre 1".equals(bookDto.getGenre().getName()) &&
                                bookDto.getId() != null
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен обновлять существующую книгу")
    void shouldUpdateExistingBook() {
        Book bookToSave = new Book(null, "Original Title", authorId1, genreId1);
        String bookId = reactiveMongoTemplate.save(bookToSave).block().getId();

        StepVerifier.create(bookService.update(bookId, "Updated Title", authorId2, genreId2))
                .expectNextMatches(bookDto ->
                        "Updated Title".equals(bookDto.getTitle()) &&
                                "Test Author 2".equals(bookDto.getAuthor().getFullName()) &&
                                "Test Genre 2".equals(bookDto.getGenre().getName())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("должен удалять книгу по ID")
    void shouldDeleteBookById() {
        Book bookToSave = new Book(null, "Book to Delete", authorId1, genreId1);
        String bookId = reactiveMongoTemplate.save(bookToSave).block().getId();

        StepVerifier.create(bookService.deleteById(bookId))
                .verifyComplete();

        StepVerifier.create(reactiveMongoTemplate.findById(bookId, Book.class))
                .verifyComplete();
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке вставить книгу с несуществующим автором")
    void shouldThrowEntityNotFoundExceptionWhenInsertingWithNonExistentAuthor() {
        StepVerifier.create(bookService.insert("Title", "non-existent-author-id", genreId1))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке вставить книгу с несуществующим жанром")
    void shouldThrowEntityNotFoundExceptionWhenInsertingWithNonExistentGenre() {
        StepVerifier.create(bookService.insert("Title", authorId1, "non-existent-genre-id"))
                .expectError(EntityNotFoundException.class)
                .verify();
    }
}