package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.controllers.rest.dto.BookCreateDto;
import ru.otus.hw.controllers.rest.dto.BookUpdateDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.services.BookService;

@RestController
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    @GetMapping("/api/v1/books")
    public Flux<Book> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/api/v1/books/{id}")
    public Mono<ResponseEntity<Book>> getBookById(@PathVariable String id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/books")
    public Mono<ResponseEntity<Book>> createBook(@Valid @RequestBody BookCreateDto bookDto) {
        return bookService.insert(bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreId())
                .map(book -> ResponseEntity.status(HttpStatus.CREATED).body(book))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PutMapping("/api/v1/books/{id}")
    public Mono<ResponseEntity<Book>> updateBook(@PathVariable String id,
                                                 @Valid @RequestBody BookUpdateDto bookDto) {
        return bookService.update(id, bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreId())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/api/v1/books/{id}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable String id) {
        return bookService.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
}