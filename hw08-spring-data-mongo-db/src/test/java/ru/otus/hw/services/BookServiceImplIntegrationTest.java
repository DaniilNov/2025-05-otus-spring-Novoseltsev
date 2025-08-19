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
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Интеграционные тесты BookService")
class BookServiceImplIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Author testAuthor1;
    private Author testAuthor2;
    private Genre testGenre1;
    private Genre testGenre2;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();

        testAuthor1 = authorRepository.save(new Author(null, "Test Author 1"));
        testAuthor2 = authorRepository.save(new Author(null, "Test Author 2"));
        testGenre1 = genreRepository.save(new Genre(null, "Test Genre 1"));
        testGenre2 = genreRepository.save(new Genre(null, "Test Genre 2"));
    }

    @Test
    @DisplayName("должен находить книгу по ID")
    void shouldFindBookById() {
        Book bookToSave = new Book(null, "Test Book", testAuthor1, testGenre1);
        Book savedBook = bookRepository.save(bookToSave);

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
        bookRepository.save(new Book(null, "Book 1", testAuthor1, testGenre1));
        bookRepository.save(new Book(null, "Book 2", testAuthor2, testGenre2));

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

        Book foundBook = bookRepository.findById(insertedBook.getId()).orElse(null);
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo("New Book");
        assertThat(foundBook.getAuthor().getId()).isEqualTo(testAuthor1.getId());
        assertThat(foundBook.getGenre().getId()).isEqualTo(testGenre1.getId());
    }

    @Test
    @DisplayName("должен обновлять существующую книгу")
    void shouldUpdateExistingBook() {
        Book bookToSave = new Book(null, "Original Title", testAuthor1, testGenre1);
        Book savedBook = bookRepository.save(bookToSave);

        Book updatedBook = bookService.update(savedBook.getId(), "Updated Title", testAuthor2.getId(), testGenre2.getId());

        assertThat(updatedBook.getId()).isEqualTo(savedBook.getId());
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");

        Book foundBook = bookRepository.findById(savedBook.getId()).orElse(null);
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo("Updated Title");
        assertThat(foundBook.getAuthor().getId()).isEqualTo(testAuthor2.getId());
        assertThat(foundBook.getGenre().getId()).isEqualTo(testGenre2.getId());
    }

    @Test
    @DisplayName("должен удалять книгу по ID")
    void shouldDeleteBookById() {
        Book bookToSave = new Book(null, "Book to Delete", testAuthor1, testGenre1);
        Book savedBook = bookRepository.save(bookToSave);

        bookService.deleteById(savedBook.getId());

        assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
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