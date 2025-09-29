package ru.otus.hw.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.rest.dto.CommentCreateDto;
import ru.otus.hw.controllers.rest.dto.CommentUpdateDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.User;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.UserServiceImpl;

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

@WebMvcTest(value = CommentRestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CommentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserServiceImpl userService;

    @Test
    @WithMockUser(username = "testuser")
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
    @WithMockUser(username = "testuser")
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
    @WithMockUser(username = "testuser")
    void getCommentByIdNonExistingCommentShouldReturnNotFound() throws Exception {
        when(commentService.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).findById("999");
    }

    @Test
    @WithMockUser(username = "testuser")
    void createCommentValidDataShouldReturnCreatedComment() throws Exception {
        Book book = new Book();
        book.setId("1");
        User author = new User();
        author.setId("author1");
        author.setUsername("testuser");

        Comment comment = new Comment("1", "New Comment", book, author);
        CommentCreateDto commentCreateDto = new CommentCreateDto("New Comment", "1");

        when(userService.loadUserByUsername("testuser")).thenReturn(author);

        when(commentService.create(eq("New Comment"), eq("1"), eq(author))).thenReturn(comment);

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("New Comment"));

        verify(commentService, times(1)).create("New Comment", "1", author);
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCommentValidDataShouldReturnUpdatedComment() throws Exception {
        Book book = new Book();
        book.setId("1");
        User user = new User();
        user.setId("user1");
        user.setUsername("testuser");

        Comment comment = new Comment("1", "Updated Comment", book, user);
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("Updated Comment");

        when(userService.loadUserByUsername("testuser")).thenReturn(user);

        when(commentService.update(eq("1"), eq("Updated Comment"), eq(user))).thenReturn(comment);

        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.text").value("Updated Comment"));

        verify(commentService, times(1)).update("1", "Updated Comment", user);
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteCommentShouldReturnNoContent() throws Exception {
        User user = new User();
        user.setId("user1");
        user.setUsername("testuser");

        when(userService.loadUserByUsername("testuser")).thenReturn(user);

        doNothing().when(commentService).deleteById("1", user);

        mockMvc.perform(delete("/api/v1/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteById("1", user);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("", "1");

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCommentInvalidDataShouldReturnBadRequest() throws Exception {
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("");

        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isBadRequest());
    }
}