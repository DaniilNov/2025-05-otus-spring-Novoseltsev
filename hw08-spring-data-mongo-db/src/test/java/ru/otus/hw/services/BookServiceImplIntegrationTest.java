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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Интеграционные тесты BookService")
class BookServiceImplIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Author testAuthor1;
    private Author testAuthor2;
    private Genre testGenre1;
    private Genre testGenre2;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();

        testAuthor1 = mongoTemplate.save(new Author(null, "Test Author 1"));
        testAuthor2 = mongoTemplate.save(new Author(null, "Test Author 2"));
        testGenre1 = mongoTemplate.save(new Genre(null, "Test Genre 1"));
        testGenre2 = mongoTemplate.save(new Genre(null, "Test Genre 2"));
    }

    @Test
    @DisplayName("должен находить книгу по ID")
    void shouldFindBookById() {
        Book bookToSave = new Book(null, "Test Book", testAuthor1, testGenre1);
        Book savedBook = mongoTemplate.save(bookToSave);

        Book foundBook = bookService.findById(savedBook.getId()).orElse(null);

        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getId()).isEqualTo(savedBook.getId());
        assertThat(foundBook.getTitle()).isEqualTo("Test Book");
        assertThat(foundBook.getAuthor()).isNotNull();
        assertThat(foundBook.getGenre()).isNotNull();
        assertThat(foundBook.getAuthor().getId()).isEqualTo(testAuthor1.getId());
        assertThat(foundBook.getGenre().getId()).isEqualTo(testGenre1.getId());
    }

    @Test
    @DisplayName("должен возвращать пустой Optional, если книга не найдена")
    void shouldReturnEmptyIfBookNotFound() {
        var result = bookService.findById("non-existent-id");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("должен находить все книги")
    void shouldFindAllBooks() {
        mongoTemplate.save(new Book(null, "Book 1", testAuthor1, testGenre1));
        mongoTemplate.save(new Book(null, "Book 2", testAuthor2, testGenre2));

        List<Book> books = bookService.findAll();

        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Book 1", "Book 2");
        assertThat(books).allSatisfy(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenre()).isNotNull();
        });
    }

    @Test
    @DisplayName("должен вставлять новую книгу")
    void shouldInsertNewBook() {
        Book insertedBook = bookService.insert("New Book", testAuthor1.getId(), testGenre1.getId());

        assertThat(insertedBook.getId()).isNotNull();
        assertThat(insertedBook.getTitle()).isEqualTo("New Book");

        Book foundBook = mongoTemplate.findById(insertedBook.getId(), Book.class);
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo("New Book");
        assertThat(foundBook.getAuthor().getId()).isEqualTo(testAuthor1.getId());
        assertThat(foundBook.getGenre().getId()).isEqualTo(testGenre1.getId());
    }

    @Test
    @DisplayName("должен обновлять существующую книгу")
    void shouldUpdateExistingBook() {
        Book bookToSave = new Book(null, "Original Title", testAuthor1, testGenre1);
        Book savedBook = mongoTemplate.save(bookToSave);

        Book updatedBook = bookService.update(savedBook.getId(), "Updated Title", testAuthor2.getId(), testGenre2.getId());

        assertThat(updatedBook.getId()).isEqualTo(savedBook.getId());
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");

        Book foundBook = mongoTemplate.findById(savedBook.getId(), Book.class);
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo("Updated Title");
        assertThat(foundBook.getAuthor().getId()).isEqualTo(testAuthor2.getId());
        assertThat(foundBook.getGenre().getId()).isEqualTo(testGenre2.getId());
    }

    @Test
    @DisplayName("должен удалять книгу по ID")
    void shouldDeleteBookById() {
        Book bookToSave = new Book(null, "Book to Delete", testAuthor1, testGenre1);
        Book savedBook = mongoTemplate.save(bookToSave);

        bookService.deleteById(savedBook.getId());

        assertThat(mongoTemplate.findById(savedBook.getId(), Book.class)).isNull();
    }

    @Test
    @DisplayName("должен удалять книгу и связанные комментарии по ID")
    void shouldDeleteBookAndAssociatedCommentsById() {
        Book bookToDelete = new Book(null, "Book to Delete with Comments", testAuthor1, testGenre1);
        bookToDelete = mongoTemplate.save(bookToDelete);

        Comment comment1 = new Comment();
        comment1.setText("Comment 1 for deleted book");
        comment1.setBook(bookToDelete);
        mongoTemplate.save(comment1);

        Comment comment2 = new Comment();
        comment2.setText("Comment 2 for deleted book");
        comment2.setBook(bookToDelete);
        mongoTemplate.save(comment2);

        assertThat(mongoTemplate.findAll(Book.class)).hasSize(1);
        assertThat(mongoTemplate.findAll(Comment.class)).hasSize(2);

        bookService.deleteById(bookToDelete.getId());

        assertThat(mongoTemplate.findById(bookToDelete.getId(), Book.class)).isNull();
        assertThat(mongoTemplate.findAll(Comment.class)).isEmpty();
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке вставить книгу с несуществующим автором")
    void shouldThrowEntityNotFoundExceptionWhenInsertingWithNonExistentAuthor() {
        assertThatThrownBy(() -> bookService.insert("Title", "non-existent-author-id", testGenre1.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id non-existent-author-id not found");
    }

    @Test
    @DisplayName("должен бросать EntityNotFoundException при попытке вставить книгу с несуществующим жанром")
    void shouldThrowEntityNotFoundExceptionWhenInsertingWithNonExistentGenre() {
        assertThatThrownBy(() -> bookService.insert("Title", testAuthor1.getId(), "non-existent-genre-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genre with id non-existent-genre-id not found");
    }
}