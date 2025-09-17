package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(AuthorRestController.class)
class AuthorRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    @Test
    void getAllAuthorsShouldReturnAuthorsList() throws Exception {
        Author author1 = new Author("1", "Author 1");
        Author author2 = new Author("2", "Author 2");
        List<Author> authors = List.of(author1, author2);

        when(authorService.findAll()).thenReturn(Flux.fromIterable(authors));

        webTestClient.get().uri("/api/v1/authors")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBodyList(Author.class)
                .hasSize(2)
                .contains(author1, author2);

        verify(authorService, times(1)).findAll();
    }
}