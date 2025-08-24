package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @Test
    void testListBooks() throws Exception {
        List<Book> books = List.of(
                new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1")),
                new Book("2", "Book2", new Author("2", "Author2"), new Genre("2", "Genre2"))
        );
        given(bookService.findAll()).willReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/list"))
                .andExpect(model().attribute("books", books));

        verify(bookService, times(1)).findAll();
    }

    @Test
    void testViewBook() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        List<ru.otus.hw.models.Comment> comments = List.of();

        given(bookService.findById("1")).willReturn(Optional.of(book));
        given(commentService.findByBookId("1")).willReturn(comments);

        mockMvc.perform(get("/books/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("comments", comments));

        verify(bookService, times(1)).findById("1");
        verify(commentService, times(1)).findByBookId("1");
    }

    @Test
    void testViewBookNotFound() throws Exception {
        given(bookService.findById("999")).willReturn(Optional.empty());

        mockMvc.perform(get("/books/view/999"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById("999");
        verify(commentService, never()).findByBookId(anyString());
    }

    @Test
    void testCreateBookForm() throws Exception {
        List<Author> authors = List.of(new Author("1", "Author1"));
        List<Genre> genres = List.of(new Genre("1", "Genre1"));
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);

        mockMvc.perform(get("/books/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/create"))
                .andExpect(model().attributeExists("authors", "genres"));

        verify(authorService, times(1)).findAll();
        verify(genreService, times(1)).findAll();
    }

    @Test
    void testCreateBook() throws Exception {
        Book savedBook = new Book("1", "New Book", new Author("1", "Author1"), new Genre("1", "Genre1"));
        given(bookService.insert(anyString(), anyString(), anyString())).willReturn(savedBook);

        mockMvc.perform(post("/books/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Book")
                        .param("authorId", "1")
                        .param("genreId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/1"));

        verify(bookService, times(1)).insert("New Book", "1", "1");
    }

    @Test
    void testEditBookForm() throws Exception {
        Book book = new Book("1", "Book1", new Author("1", "Author1"), new Genre("1", "Genre1"));
        List<Author> authors = List.of(new Author("1", "Author1"), new Author("2", "Author2"));
        List<Genre> genres = List.of(new Genre("1", "Genre1"), new Genre("2", "Genre2"));

        given(bookService.findById("1")).willReturn(Optional.of(book));
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);

        mockMvc.perform(get("/books/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/edit"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));

        verify(bookService, times(1)).findById("1");
        verify(authorService, times(1)).findAll();
        verify(genreService, times(1)).findAll();
    }

    @Test
    void testEditBookFormNotFound() throws Exception {
        given(bookService.findById("999")).willReturn(Optional.empty());

        mockMvc.perform(get("/books/edit/999"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById("999");
        verify(authorService, never()).findAll();
        verify(genreService, never()).findAll();
    }

    @Test
    void testEditBook() throws Exception {
        Book updatedBook = new Book("1", "Updated Book", new Author("2", "Author2"), new Genre("2", "Genre2"));
        given(bookService.update(anyString(), anyString(), anyString(), anyString())).willReturn(updatedBook);

        mockMvc.perform(post("/books/edit/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Book")
                        .param("authorId", "2")
                        .param("genreId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/1"));

        verify(bookService, times(1)).update("1", "Updated Book", "2", "2");
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(post("/books/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService, times(1)).deleteById("1");
    }
}