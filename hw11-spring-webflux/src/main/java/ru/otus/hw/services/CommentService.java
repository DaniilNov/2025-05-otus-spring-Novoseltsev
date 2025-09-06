package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

public interface CommentService {
    Mono<Comment> findById(String id);

    Mono<CommentDto> findDtoById(String id);

    Flux<Comment> findByBookId(String bookId);

    Flux<CommentDto> findDtosByBookId(String bookId);

    Mono<Comment> create(String text, String bookId);

    Mono<Comment> update(String id, String text);

    Mono<Void> deleteById(String id);
}
