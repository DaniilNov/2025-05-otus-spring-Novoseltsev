package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DisplayName("Интеграционные тесты BookService")
class BookServiceImplIntegrationTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("При загрузке книги по ID должны загружаться автор и жанр без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldLoadBookWithRelatedEntitiesWithoutLazyException() {
        Book book = bookService.findById(1L).orElseThrow();

        assertThat(book.getTitle()).isEqualTo("BookTitle_1");
        assertThat(book.getAuthor())
                .isNotNull()
                .extracting(Author::getFullName)
                .isEqualTo("Author_1");
        assertThat(book.getGenre())
                .isNotNull()
                .extracting(Genre::getName)
                .isEqualTo("Genre_1");
    }

    @Test
    @DisplayName("при загрузке всех книг должны загружать связанные сущности без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldLoadAllBooksWithoutLazyException() {
        List<Book> books = bookService.findAll();

        assertThat(books)
                .hasSize(3)
                .allSatisfy(book -> {
                    assertThat(book.getAuthor()).isNotNull();
                    assertThat(book.getGenre()).isNotNull();
                })
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder(
                        "BookTitle_1",
                        "BookTitle_2",
                        "BookTitle_3"
                );
    }
}