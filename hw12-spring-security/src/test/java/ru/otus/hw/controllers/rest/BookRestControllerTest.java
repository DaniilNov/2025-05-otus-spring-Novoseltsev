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
import ru.otus.hw.controllers.rest.dto.BookCreateDto;
import ru.otus.hw.controllers.rest.dto.BookUpdateDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;

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

@WebMvcTest(BookRestController.class)
@Import(SecurityConfiguration.class)
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    @WithMockUser
    void getAllBooksShouldReturnBooksList() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book1 = new Book("1", "Book Title 1", author, genre);
        Book book2 = new Book("2", "Book Title 2", author, genre);
        List<Book> books = List.of(book1, book2);

        when(bookService.findAll()).thenReturn(books);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("Book Title 1"));

        verify(bookService, times(1)).findAll();
    }

    @Test
    @WithMockUser
    void getBookByIdExistingBookShouldReturnBook() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Book Title", author, genre);

        when(bookService.findById("1")).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Book Title"));

        verify(bookService, times(1)).findById("1");
    }

    @Test
    @WithMockUser
    void getBookByIdNonExistingBookShouldReturnNotFound() throws Exception {
        when(bookService.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/books/999"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById("999");
    }

    @Test
    @WithMockUser
    void createBookValidDataShouldReturnCreatedBook() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "New Book", author, genre);
        BookCreateDto bookCreateDto = new BookCreateDto("New Book", "1", "1");

        when(bookService.insert(eq("New Book"), eq("1"), eq("1"))).thenReturn(book);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("New Book"));

        verify(bookService, times(1)).insert("New Book", "1", "1");
    }

    @Test
    @WithMockUser
    void createBookInvalidDataShouldReturnBadRequest() throws Exception {
        BookCreateDto bookCreateDto = new BookCreateDto("", "1", "1");

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateBookValidDataShouldReturnUpdatedBook() throws Exception {
        Author author = new Author("1", "Author Name");
        Genre genre = new Genre("1", "Genre Name");
        Book book = new Book("1", "Updated Book", author, genre);
        BookUpdateDto bookUpdateDto = new BookUpdateDto("Updated Book", "1", "1");

        when(bookService.update(eq("1"), eq("Updated Book"), eq("1"), eq("1"))).thenReturn(book);

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Updated Book"));

        verify(bookService, times(1)).update("1", "Updated Book", "1", "1");
    }

    @Test
    @WithMockUser
    void deleteBookShouldReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteById("1");

        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteById("1");
    }
}