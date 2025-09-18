package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.controllers.rest.dto.BookCreateDto;
import ru.otus.hw.controllers.rest.dto.BookUpdateDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    @GetMapping("/api/v1/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/api/v1/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        Optional<Book> book = bookService.findById(id);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/books")
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookCreateDto bookDto) {
        Book book = bookService.insert(bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreId());
        return ResponseEntity.status(201).body(book);
    }

    @PutMapping("/api/v1/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @Valid @RequestBody BookUpdateDto bookDto) {
        Book book = bookService.update(id, bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreId());
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/api/v1/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}