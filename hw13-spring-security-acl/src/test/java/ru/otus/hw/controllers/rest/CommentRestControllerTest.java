package ru.otus.hw.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.rest.dto.CommentCreateDto;
import ru.otus.hw.controllers.rest.dto.CommentUpdateDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.User;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserService userService;

    @Test
    void getCommentsByBookIdShouldReturnCommentsList() throws Exception {
        Book book = new Book();
        book.setId("1");
        User user = new User();
        user.setId("user1");
        Comment comment1 = new Comment("1", "Comment 1", book, user);
        Comment comment2 = new Comment("2", "Comment 2", book, user);
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
    void getCommentByIdExistingCommentShouldReturnComment() throws Exception {
        Book book = new Book();
        book.setId("1");
        User user = new User();
        user.setId("user1");
        Comment comment = new Comment("1", "Comment Text", book, user);

        when(commentService.findById("1")).thenReturn(Optional.of(comment));

        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("Comment Text"))
                .andExpect(jsonPath("$.book.id").value("1"))
                .andExpect(jsonPath("$.user.id").value("user1"));

        verify(commentService, times(1)).findById("1");
    }

    @Test
    void getCommentByIdNonExistingCommentShouldReturnNotFound() throws Exception {
        when(commentService.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).findById("999");
    }

    @Test
    void createCommentValidDataShouldReturnCreatedComment() throws Exception {
        String bookId = "1";
        Book book = new Book();
        book.setId(bookId);

        User testUser = new User();
        testUser.setId("user1");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole("USER");

        Comment comment = new Comment("1", "New Comment", book, testUser);
        CommentCreateDto commentCreateDto = new CommentCreateDto("New Comment", bookId);

        when(commentService.create(any(), any(), any())).thenReturn(comment);

        mockMvc.perform(post("/api/v1/comments")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("New Comment"))
                .andExpect(jsonPath("$.book.id").value("1"))
                .andExpect(jsonPath("$.user.id").value("user1"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(commentService, times(1)).create(any(), any(), any());
    }

    @Test
    void updateCommentValidDataShouldReturnUpdatedComment() throws Exception {
        String commentId = "1";
        String newText = "Updated Comment";

        Book book = new Book();
        book.setId("1");

        User userInComment = new User();
        userInComment.setId("someUserId");
        Comment comment = new Comment(commentId, newText, book, userInComment);
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto(newText);

        when(commentService.update(eq(commentId), eq(newText))).thenReturn(comment);

        mockMvc.perform(put("/api/v1/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value(newText));

        verify(commentService, times(1)).update(commentId, newText);
    }

    @Test
    void deleteCommentShouldReturnNoContent() throws Exception {
        String commentId = "1";

        doNothing().when(commentService).deleteById(commentId);

        mockMvc.perform(delete("/api/v1/comments/" + commentId))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteById(commentId);
    }

    @Test
    void createCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("", "1");

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("");

        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isBadRequest());
    }
}