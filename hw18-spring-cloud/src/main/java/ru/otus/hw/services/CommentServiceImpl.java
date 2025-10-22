package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ServiceTemporarilyUnavailableException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final ResilientExecutor resilientExecutor;

    @Transactional(readOnly = true)
    @Override
    public Optional<Comment> findById(String id) {
        return resilientExecutor.executeOrFallback(() -> commentRepository.findById(id), Optional::empty);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> findByBookId(String bookId) {
        return resilientExecutor.executeOrFallback(() -> commentRepository.findByBookId(bookId), Collections::emptyList);
    }

    @Transactional
    @Override
    public Comment create(String text, String bookId) {
        Book book = resilientExecutor.executeOrFallback(
                () -> bookRepository.findById(bookId)
                        .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId))),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Book lookup is temporarily unavailable");
                }
        );
        Comment comment = new Comment();
        comment.setText(text);
        comment.setBook(book);
        return resilientExecutor.executeOrFallback(
                () -> commentRepository.save(comment),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Saving comment is temporarily unavailable");
                }
        );
    }

    @Transactional
    @Override
    public Comment update(String id, String text) {
        Comment comment = resilientExecutor.executeOrFallback(
                () -> commentRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id))),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Comment lookup is temporarily unavailable");
                }
        );
        comment.setText(text);
        return resilientExecutor.executeOrFallback(
                () -> commentRepository.save(comment),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Saving comment is temporarily unavailable");
                }
        );
    }

    @Transactional
    @Override
    public void deleteById(String id) {
        resilientExecutor.executeVoidOrFallback(
                () -> commentRepository.deleteById(id),
                () -> {
                    throw new ServiceTemporarilyUnavailableException("Deleting comment is temporarily unavailable");
                }
        );
    }
}