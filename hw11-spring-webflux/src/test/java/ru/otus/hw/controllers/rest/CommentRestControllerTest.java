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
import ru.otus.hw.controllers.rest.dto.CommentCreateDto;
import ru.otus.hw.controllers.rest.dto.CommentUpdateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CommentRestController.class)
class CommentRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void getCommentsByBookIdShouldReturnCommentsList() {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Book Title", "1", "1");
        CommentDto commentDto1 = new CommentDto("1", "Comment 1", book);
        CommentDto commentDto2 = new CommentDto("2", "Comment 2", book);
        List<CommentDto> commentDtos = List.of(commentDto1, commentDto2);

        when(commentService.findDtosByBookId("1")).thenReturn(Flux.fromIterable(commentDtos));

        webTestClient.get().uri("/api/v1/comments/book/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBodyList(CommentDto.class)
                .hasSize(2)
                .contains(commentDto1, commentDto2);

        verify(commentService, times(1)).findDtosByBookId("1");
    }

    @Test
    void getCommentByIdExistingCommentShouldReturnComment() {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Book Title", "1", "1");
        CommentDto commentDto = new CommentDto("1", "Comment Text", book);

        when(commentService.findDtoById("1")).thenReturn(Mono.just(commentDto));

        webTestClient.get().uri("/api/v1/comments/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(CommentDto.class)
                .isEqualTo(commentDto);

        verify(commentService, times(1)).findDtoById("1");
    }

    @Test
    void getCommentByIdNonExistingCommentShouldReturnNotFound(){
        when(commentService.findDtoById("999")).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/comments/999")
                .exchange()
                .expectStatus().isNotFound();

        verify(commentService, times(1)).findDtoById("999");
    }

    @Test
    void createCommentValidDataShouldReturnCreatedComment() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Book Title", "1", "1");
        CommentDto commentDto = new CommentDto("1", "New Comment", book);
        CommentCreateDto commentCreateDto = new CommentCreateDto("New Comment", "1");

        when(commentService.create(eq("New Comment"), eq("1"))).thenReturn(Mono.just(new ru.otus.hw.models.Comment("1", "New Comment", "1")));
        when(commentService.findDtoById("1")).thenReturn(Mono.just(commentDto));

        webTestClient.post().uri("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(commentCreateDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommentDto.class)
                .isEqualTo(commentDto);

        verify(commentService, times(1)).create("New Comment", "1");
        verify(commentService, times(1)).findDtoById("1");
    }

    @Test
    void updateCommentValidDataShouldReturnUpdatedComment() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Book Title", "1", "1");
        CommentDto commentDto = new CommentDto("1", "Updated Comment", book);
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("Updated Comment");

        when(commentService.update(eq("1"), eq("Updated Comment"))).thenReturn(Mono.just(new ru.otus.hw.models.Comment("1", "Updated Comment", "1")));
        when(commentService.findDtoById("1")).thenReturn(Mono.just(commentDto));

        webTestClient.put().uri("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(commentUpdateDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommentDto.class)
                .isEqualTo(commentDto);

        verify(commentService, times(1)).update("1", "Updated Comment");
        verify(commentService, times(1)).findDtoById("1");
    }

    @Test
    void deleteCommentShouldReturnNoContent() throws Exception {
        when(commentService.deleteById("1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/comments/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(commentService, times(1)).deleteById("1");
    }

    @Test
    void createCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("", "1");

        webTestClient.post().uri("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(commentCreateDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("");

        webTestClient.put().uri("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(commentUpdateDto))
                .exchange()
                .expectStatus().isBadRequest();
    }
}