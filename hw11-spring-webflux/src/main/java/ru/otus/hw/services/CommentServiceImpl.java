package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<Comment> findById(String id) {
        return commentRepository.findCommentById(id);
    }

    @Override
    public Flux<Comment> findByBookId(String bookId) {
        return commentRepository.findCommentsByBookId(bookId);
    }

    @Override
    public Mono<Comment> create(String text, String bookId) {
        return bookRepository.findBookById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %s not found".formatted(bookId))))
                .flatMap(book -> {
                    Comment comment = new Comment(null, text, book);
                    return commentRepository.saveComment(comment);
                });
    }

    @Override
    public Mono<Comment> update(String id, String text) {
        return commentRepository.findCommentById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %s not found".formatted(id))))
                .flatMap(comment -> {
                    comment.setText(text);
                    return commentRepository.saveComment(comment);
                });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return commentRepository.deleteById(id);
    }
}