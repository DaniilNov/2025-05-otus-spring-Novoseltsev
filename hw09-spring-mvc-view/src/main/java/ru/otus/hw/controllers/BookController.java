package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        return "book/list";
    }

    @GetMapping("/view/{id}")
    public String viewBook(@PathVariable String id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));

        List<Comment> comments = commentService.findByBookId(id);

        model.addAttribute("book", book);
        model.addAttribute("comments", comments);

        return "book/view";
    }

    @PostMapping("/create")
    public String createBook(@RequestParam("title") String title,
                             @RequestParam("authorId") String authorId,
                             @RequestParam("genreId") String genreId) {
        Book savedBook = bookService.insert(title, authorId, genreId);
        return "redirect:/books/view/" + savedBook.getId();
    }

    @GetMapping("/create")
    public String createBookForm(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/create";
    }

    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable String id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/edit";
    }

    @PostMapping("/edit/{id}")
    public String editBook(@PathVariable String id,
                           @RequestParam("title") String title,
                           @RequestParam("authorId") String authorId,
                           @RequestParam("genreId") String genreId) {
        Book updatedBook = bookService.update(id, title, authorId, genreId);
        return "redirect:/books/view/" + updatedBook.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable String id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }
}