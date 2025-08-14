package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@DisplayName("Репозиторий на основе JPA для работы с книгами")
class JpaBookRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookRepository repository;

    @Test
    @DisplayName("должен загружать книгу по id с информацией об авторе и жанре")
    void shouldFindBookByIdWithAuthorAndGenre() {
        Author author = em.persist(new Author(0, "Test Author"));
        Genre genre = em.persist(new Genre(0, "Test Genre"));
        Book expected = em.persist(new Book(0, "Test Book", author, genre));
        em.flush();
        em.clear();

        Optional<Book> actual = repository.findById(expected.getId());

        assertThat(actual)
                .isPresent()
                .get()
                .satisfies(book -> {
                    assertThat(book.getTitle()).isEqualTo("Test Book");
                    assertThat(book.getAuthor().getFullName()).isEqualTo("Test Author");
                    assertThat(book.getGenre().getName()).isEqualTo("Test Genre");
                });
    }

    @Test
    @DisplayName("должен загружать список всех книг с авторами и жанрами")
    void shouldFindAllBooksWithAuthorsAndGenres() {

        EntityManager entityManager = em.getEntityManager();
        entityManager.createQuery("DELETE FROM Comment").executeUpdate();
        entityManager.createQuery("DELETE FROM Book").executeUpdate();
        entityManager.createQuery("DELETE FROM Author").executeUpdate();
        entityManager.createQuery("DELETE FROM Genre").executeUpdate();
        em.flush();

        Author author = em.persist(new Author(0, "Author"));
        Genre genre = em.persist(new Genre(0, "Genre"));
        Book book1 = em.persist(new Book(0, "Book 1", author, genre));
        Book book2 = em.persist(new Book(0, "Book 2", author, genre));
        em.flush();
        em.clear();

        List<Book> books = repository.findAll();

        assertThat(books)
                .hasSize(2)
                .extracting(
                        Book::getTitle,
                        b -> b.getAuthor().getFullName(),
                        b -> b.getGenre().getName())
                .containsExactlyInAnyOrder(
                        tuple("Book 1", "Author", "Genre"),
                        tuple("Book 2", "Author", "Genre"));
    }

    @Test
    @DisplayName("должен сохранять новую книгу")
    void shouldSaveNewBook() {
        Author author = em.persist(new Author(0, "Author"));
        Genre genre = em.persist(new Genre(0, "Genre"));
        em.flush();

        Book newBook = new Book(0, "New Book", author, genre);
        Book saved = repository.save(newBook);
        em.flush();
        em.clear();

        Book found = em.find(Book.class, saved.getId());
        assertThat(found)
                .extracting(
                        Book::getTitle,
                        b -> b.getAuthor().getFullName(),
                        b -> b.getGenre().getName())
                .containsExactly("New Book", "Author", "Genre");
    }

    @Test
    @DisplayName("должен обновлять существующую книгу")
    void shouldUpdateExistingBook() {
        Author author = em.persist(new Author(0, "Author"));
        Genre genre = em.persist(new Genre(0, "Genre"));
        Book existing = em.persist(new Book(0, "Existing Book", author, genre));
        em.flush();

        existing.setTitle("Updated Book");
        Book updated = repository.save(existing);
        em.flush();
        em.clear();

        Book found = em.find(Book.class, updated.getId());
        assertThat(found.getTitle()).isEqualTo("Updated Book");
    }

    @Test
    @DisplayName("должен удалять книгу по id")
    void shouldDeleteBookById() {
        Author author = em.persist(new Author(0, "Author"));
        Genre genre = em.persist(new Genre(0, "Genre"));
        Book book = em.persist(new Book(0, "To Delete", author, genre));
        em.flush();

        repository.deleteById(book.getId());
        em.flush();

        assertThat(em.find(Book.class, book.getId())).isNull();
    }

    @Test
    @DisplayName("не должен бросать исключение при удалении несуществующей книги")
    void shouldNotThrowWhenDeletingNonExistingBook() {
        assertThatNoException().isThrownBy(() -> repository.deleteById(999L));
    }
}