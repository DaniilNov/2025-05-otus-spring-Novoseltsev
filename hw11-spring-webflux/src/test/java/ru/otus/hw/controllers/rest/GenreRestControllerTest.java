package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(GenreRestController.class)
class GenreRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GenreService genreService;

    @Test
    void getAllGenresShouldReturnGenresList() throws Exception {
        Genre genre1 = new Genre("1", "Genre 1");
        Genre genre2 = new Genre("2", "Genre 2");
        List<Genre> genres = List.of(genre1, genre2);

        when(genreService.findAll()).thenReturn(Flux.fromIterable(genres));

        webTestClient.get().uri("/api/v1/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBodyList(Genre.class)
                .hasSize(2)
                .contains(genre1, genre2);

        verify(genreService, times(1)).findAll();
    }
}