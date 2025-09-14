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
import ru.otus.hw.controllers.rest.dto.CommentCreateDto;
import ru.otus.hw.controllers.rest.dto.CommentUpdateDto;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.CommentService;

@RestController
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping("/api/v1/comments/book/{bookId}")
    public Flux<Comment> getCommentsByBookId(@PathVariable String bookId) {
        return commentService.findByBookId(bookId);
    }

    @GetMapping("/api/v1/comments/{id}")
    public Mono<ResponseEntity<Comment>> getCommentById(@PathVariable String id) {
        return commentService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/comments")
    public Mono<ResponseEntity<Comment>> createComment(@Valid @RequestBody CommentCreateDto commentDto) {
        return commentService.create(commentDto.getText(), commentDto.getBookId())
                .map(comment -> ResponseEntity.status(HttpStatus.CREATED).body(comment))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PutMapping("/api/v1/comments/{id}")
    public Mono<ResponseEntity<Comment>> updateComment(@PathVariable String id,
                                                       @Valid @RequestBody CommentUpdateDto commentDto) {
        return commentService.update(id, commentDto.getText())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public Mono<ResponseEntity<Void>> deleteComment(@PathVariable String id) {
        return commentService.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
}