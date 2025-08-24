package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BookService bookService;

    @Test
    void testViewComment() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        Comment comment = new Comment("10", "Great comment!", book);
        given(commentService.findById("10")).willReturn(Optional.of(comment));

        mockMvc.perform(get("/comments/view/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/view"))
                .andExpect(model().attribute("comment", comment));
    }

    @Test
    void testViewCommentNotFound() throws Exception {
        given(commentService.findById("999")).willReturn(Optional.empty());

        mockMvc.perform(get("/comments/view/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCommentForm() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        given(bookService.findById("1")).willReturn(Optional.of(book));

        mockMvc.perform(get("/comments/create").param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/create"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("bookId", "1"));
    }

    @Test
    void testCreateCommentFormBookNotFound() throws Exception {
        given(bookService.findById("999")).willReturn(Optional.empty());

        mockMvc.perform(get("/comments/create").param("bookId", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateComment() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        Comment savedComment = new Comment("10", "New comment!", book);
        given(commentService.create(anyString(), anyString())).willReturn(savedComment);

        mockMvc.perform(post("/comments/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "New comment!")
                        .param("bookId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/1"));

        verify(commentService, times(1)).create("New comment!", "1");
    }

    @Test
    void testEditCommentForm() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        Comment comment = new Comment("10", "Original comment!", book);
        given(commentService.findById("10")).willReturn(Optional.of(comment));

        mockMvc.perform(get("/comments/edit/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/edit"))
                .andExpect(model().attribute("comment", comment));
    }

    @Test
    void testEditCommentFormNotFound() throws Exception {
        given(commentService.findById("999")).willReturn(Optional.empty());

        mockMvc.perform(get("/comments/edit/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEditComment() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        Comment updatedComment = new Comment("10", "Updated comment!", book);
        given(commentService.update(anyString(), anyString())).willReturn(updatedComment);

        mockMvc.perform(post("/comments/edit/10")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "Updated comment!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/1"));

        verify(commentService, times(1)).update("10", "Updated comment!");
    }

    @Test
    void testDeleteComment() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        Comment comment = new Comment("10", "Comment to delete!", book);
        given(commentService.findById("10")).willReturn(Optional.of(comment));

        mockMvc.perform(post("/comments/delete/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/1"));

        verify(commentService, times(1)).deleteById("10");
    }

    @Test
    void testDeleteCommentNotFound() throws Exception {
        given(commentService.findById("999")).willReturn(Optional.empty());

        mockMvc.perform(post("/comments/delete/999"))
                .andExpect(status().isNotFound());
    }
}