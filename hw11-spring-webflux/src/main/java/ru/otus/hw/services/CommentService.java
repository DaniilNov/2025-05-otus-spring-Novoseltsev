package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentService {
    Mono<Comment> findById(String id);

    Flux<Comment> findByBookId(String bookId);

    Mono<Comment> create(String text, String bookId);

    Mono<Comment> update(String id, String text);

    Mono<Void> deleteById(String id);
}
