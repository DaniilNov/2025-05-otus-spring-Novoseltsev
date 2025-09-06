package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
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
        return commentRepository.findById(id);
    }

    @Override
    public Mono<CommentDto> findDtoById(String id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %s not found".formatted(id))))
                .flatMap(this::convertToDto);
    }

    @Override
    public Flux<Comment> findByBookId(String bookId) {
        return commentRepository.findByBookId(bookId);
    }

    @Override
    public Flux<CommentDto> findDtosByBookId(String bookId) {
        return commentRepository.findByBookId(bookId)
                .flatMap(this::convertToDto);
    }

    @Override
    public Mono<Comment> create(String text, String bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %s not found".formatted(bookId))))
                .flatMap(book -> {
                    Comment comment = new Comment();
                    comment.setText(text);
                    comment.setBookId(bookId);
                    return commentRepository.save(comment);
                });
    }

    @Override
    public Mono<Comment> update(String id, String text) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %s not found".formatted(id))))
                .flatMap(comment -> {
                    comment.setText(text);
                    return commentRepository.save(comment);
                });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return commentRepository.deleteById(id);
    }

    private Mono<CommentDto> convertToDto(Comment comment) {
        return bookRepository.findById(comment.getBookId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %s not found".formatted(comment.getBookId()))))
                .map(book -> {
                    CommentDto dto = new CommentDto();
                    dto.setId(comment.getId());
                    dto.setText(comment.getText());
                    dto.setBook(book);
                    return dto;
                });
    }
}