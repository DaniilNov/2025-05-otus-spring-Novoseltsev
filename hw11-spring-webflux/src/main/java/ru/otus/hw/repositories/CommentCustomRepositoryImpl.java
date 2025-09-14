package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.projections.CommentProjection;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final CommentProjectionRepository commentProjectionRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<Comment> findCommentById(String id) {
        return commentProjectionRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %s not found".formatted(id))))
                .flatMap(this::convertToComment);
    }

    @Override
    public Flux<Comment> findCommentsByBookId(String bookId) {
        return commentProjectionRepository.findByBookId(bookId)
                .flatMap(this::convertToComment);
    }

    @Override
    public Flux<Comment> findAllComments() {
        return commentProjectionRepository.findAll()
                .flatMap(this::convertToComment);
    }

    @Override
    public Mono<Comment> saveComment(Comment comment) {
        CommentProjection projection = convertToProjection(comment);
        return commentProjectionRepository.save(projection)
                .flatMap(this::convertToComment);
    }

    private Mono<Comment> convertToComment(CommentProjection projection) {
        return bookRepository.findBookById(projection.getBookId())
                .map(book -> new Comment(
                        projection.getId(),
                        projection.getText(),
                        book
                ));
    }

    private CommentProjection convertToProjection(Comment comment) {
        return new CommentProjection(
                comment.getId(),
                comment.getText(),
                comment.getBook().getId()
        );
    }
}
