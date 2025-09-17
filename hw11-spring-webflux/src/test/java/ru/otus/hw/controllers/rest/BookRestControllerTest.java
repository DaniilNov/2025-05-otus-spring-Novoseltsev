package ru.otus.hw.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.controllers.rest.dto.BookCreateDto;
import ru.otus.hw.controllers.rest.dto.BookUpdateDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(BookRestController.class)
class BookRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void getAllBooksShouldReturnBooksList() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book1 = new Book("1", "Book Title 1", author, genre);
        Book book2 = new Book("2", "Book Title 2", author, genre);

        when(bookService.findAll()).thenReturn(Flux.just(book1, book2));

        webTestClient.get().uri("/api/v1/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBodyList(Book.class)
                .hasSize(2)
                .contains(book1, book2);

        verify(bookService, times(1)).findAll();
    }

    @Test
    void getBookByIdExistingBookShouldReturnBook() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Book Title", author, genre);

        when(bookService.findById("1")).thenReturn(Mono.just(book));

        webTestClient.get().uri("/api/v1/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(Book.class)
                .isEqualTo(book);

        verify(bookService, times(1)).findById("1");
    }

    @Test
    void getBookByIdNonExistingBookShouldReturnNotFound() throws Exception {
        when(bookService.findById("999")).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/books/999")
                .exchange()
                .expectStatus().isNotFound();

        verify(bookService, times(1)).findById("999");
    }

    @Test
    void createBookValidDataShouldReturnCreatedBook() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "New Book", author, genre);
        BookCreateDto bookCreateDto = new BookCreateDto("New Book", "1", "1");

        when(bookService.insert(eq("New Book"), eq("1"), eq("1"))).thenReturn(Mono.just(book));

        webTestClient.post().uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(bookCreateDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class)
                .isEqualTo(book);

        verify(bookService, times(1)).insert("New Book", "1", "1");
    }

    @Test
    void createBookInvalidDataShouldReturnBadRequest() throws Exception {
        BookCreateDto bookCreateDto = new BookCreateDto("", "1", "1");

        webTestClient.post().uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(bookCreateDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateBookValidDataShouldReturnUpdatedBook() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Updated Book", author, genre);
        BookUpdateDto bookUpdateDto = new BookUpdateDto("Updated Book", "1", "1");

        when(bookService.update(eq("1"), eq("Updated Book"), eq("1"), eq("1"))).thenReturn(Mono.just(book));

        webTestClient.put().uri("/api/v1/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(bookUpdateDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .isEqualTo(book);

        verify(bookService, times(1)).update("1", "Updated Book", "1", "1");
    }

    @Test
    void deleteBookShouldReturnNoContent() throws Exception {
        when(bookService.deleteById("1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/books/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService, times(1)).deleteById("1");
    }
}