package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DisplayName("Интеграционные тесты BookService")
class BookServiceImplIntegrationTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("при загрузке книги должен корректно загружать автора без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldLoadBookWithRelatedEntitiesWithoutLazyException() {
        Book book = bookService.findById(1L).orElseThrow();

        assertThat(book.getTitle()).isEqualTo("BookTitle_1");
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getGenre()).isNotNull();
    }

    @Test
    @DisplayName("при загрузке всех книг должен корректно загружать связанные сущности без LazyInitializationException")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldLoadAllBooksWithoutLazyException() {
        List<Book> books = bookService.findAll();

        assertThat(books).isNotEmpty();
        books.forEach(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenre()).isNotNull();
        });
    }
}