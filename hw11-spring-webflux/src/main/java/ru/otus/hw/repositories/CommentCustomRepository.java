package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

public interface CommentCustomRepository {
    Mono<Comment> findCommentById(String id);

    Flux<Comment> findCommentsByBookId(String bookId);

    Flux<Comment> findAllComments();

    Mono<Comment> saveComment(Comment comment);
}
