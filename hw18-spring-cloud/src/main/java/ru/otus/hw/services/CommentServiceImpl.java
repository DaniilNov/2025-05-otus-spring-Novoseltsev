package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "findByIdFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public Optional<Comment> findById(String id) {
        log.debug("Finding comment by id: {}", id);
        return commentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "findByBookIdFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public List<Comment> findByBookId(String bookId) {
        log.debug("Finding comments by book id: {}", bookId);
        return commentRepository.findByBookId(bookId);
    }

    @Transactional
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "createFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public Comment create(String text, String bookId) {
        log.debug("Creating comment for book id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        Comment comment = new Comment();
        comment.setText(text);
        comment.setBook(book);
        return commentRepository.save(comment);
    }

    @Transactional
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "updateFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public Comment update(String id, String text) {
        log.debug("Updating comment id: {}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setText(text);
        return commentRepository.save(comment);
    }

    @Transactional
    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "deleteFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public void deleteById(String id) {
        log.debug("Deleting comment by id: {}", id);
        commentRepository.deleteById(id);
    }

    public Optional<Comment> findByIdFallback(String id, Throwable t) {
        log.error("Fallback for findCommentById({}): {}", id, t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Comment lookup is temporarily unavailable. Please try again later.",
                t
        );
    }

    public List<Comment> findByBookIdFallback(String bookId, Throwable t) {
        log.error("Fallback for findByBookId({}): {}", bookId, t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Comments list is temporarily unavailable. Please try again later.",
                t
        );
    }

    public Comment createFallback(String text, String bookId, Throwable t) {
        log.error("Fallback for create comment: {}", t.toString());
        EntityNotFoundException enf = findEntityNotFound(t);
        if (enf != null) {
            throw enf;
        }
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Saving comment is temporarily unavailable. Please try again later.",
                t
        );
    }

    public Comment updateFallback(String id, String text, Throwable t) {
        log.error("Fallback for update comment({}): {}", id, t.toString());
        EntityNotFoundException enf = findEntityNotFound(t);
        if (enf != null) {
            throw enf;
        }
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Updating comment is temporarily unavailable. Please try again later.",
                t
        );
    }

    public void deleteFallback(String id, Throwable t) {
        log.error("Fallback for deleteComment({}): {}", id, t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Deleting comment is temporarily unavailable. Please try again later.",
                t
        );
    }

    private static EntityNotFoundException findEntityNotFound(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof EntityNotFoundException enf) {
                return enf;
            }
            Throwable next = cur.getCause();
            if (next == cur) {
                break;
            }
            cur = next;
        }
        return null;
    }
}