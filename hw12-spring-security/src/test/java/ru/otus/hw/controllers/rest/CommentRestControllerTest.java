package ru.otus.hw.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfiguration;
import ru.otus.hw.controllers.rest.dto.CommentCreateDto;
import ru.otus.hw.controllers.rest.dto.CommentUpdateDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.CommentService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentRestController.class)
@Import(SecurityConfiguration.class)
class CommentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    @WithMockUser
    void getCommentsByBookIdShouldReturnCommentsList() throws Exception {
        Book book = new Book();
        book.setId("1");
        Comment comment1 = new Comment("1", "Comment 1", book);
        Comment comment2 = new Comment("2", "Comment 2", book);
        List<Comment> comments = List.of(comment1, comment2);

        when(commentService.findByBookId("1")).thenReturn(comments);

        mockMvc.perform(get("/api/v1/comments/book/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].text").value("Comment 1"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].text").value("Comment 2"));

        verify(commentService, times(1)).findByBookId("1");
    }

    @Test
    @WithMockUser
    void getCommentByIdExistingCommentShouldReturnComment() throws Exception {
        Book book = new Book();
        book.setId("1");
        Comment comment = new Comment("1", "Comment Text", book);

        when(commentService.findById("1")).thenReturn(Optional.of(comment));

        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("Comment Text"))
                .andExpect(jsonPath("$.book.id").value("1"));

        verify(commentService, times(1)).findById("1");
    }

    @Test
    @WithMockUser
    void getCommentByIdNonExistingCommentShouldReturnNotFound() throws Exception {
        when(commentService.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).findById("999");
    }

    @Test
    @WithMockUser
    void createCommentValidDataShouldReturnCreatedComment() throws Exception {
        Book book = new Book();
        book.setId("1");
        Comment comment = new Comment("1", "New Comment", book);
        CommentCreateDto commentCreateDto = new CommentCreateDto("New Comment", "1");

        when(commentService.create(eq("New Comment"), eq("1"))).thenReturn(comment);

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("New Comment"));

        verify(commentService, times(1)).create("New Comment", "1");
    }

    @Test
    @WithMockUser
    void updateCommentValidDataShouldReturnUpdatedComment() throws Exception {
        Book book = new Book();
        book.setId("1");
        Comment comment = new Comment("1", "Updated Comment", book);
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("Updated Comment");

        when(commentService.update(eq("1"), eq("Updated Comment"))).thenReturn(comment);

        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("Updated Comment"));

        verify(commentService, times(1)).update("1", "Updated Comment");
    }

    @Test
    @WithMockUser
    void deleteCommentShouldReturnNoContent() throws Exception {
        doNothing().when(commentService).deleteById("1");

        mockMvc.perform(delete("/api/v1/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteById("1");
    }

    @Test
    @WithMockUser
    void createCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("", "1");

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("");

        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isBadRequest());
    }
}