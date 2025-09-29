package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final UserRepository userRepository;

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Comment> findById(String id) {
        return commentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> findByBookId(String bookId) {
        return commentRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> findByUser(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return commentRepository.findByUserId(user.getId());
    }

    @Transactional
    @Override
    public Comment create(String text, String bookId, User author) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        if (author == null || author.getId() == null) {
            throw new IllegalArgumentException("Author user cannot be null or without ID");
        }
        Comment comment = new Comment();
        comment.setText(text);
        comment.setBook(book);
        comment.setUser(author);
        return commentRepository.save(comment);
    }

    @Transactional
    @Override
    public Comment update(String id, String text, User user) {
        if (!isAdmin() && !isOwner(id, user.getId())) {
            throw new EntityNotFoundException("Comment with id %s not found or not owned by user %s"
                    .formatted(id, user.getId()));
        }
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setText(text);
        return commentRepository.save(comment);
    }

    @Transactional
    @Override
    public void deleteById(String id, User user) {
        if (!isAdmin() && !isOwner(id, user.getId())) {
            throw new EntityNotFoundException("Comment with id %s not found or not owned by user %s"
                    .formatted(id, user.getId()));
        }
        commentRepository.deleteById(id);
    }

    @Override
    public boolean isOwner(String commentId, String userId) {
        return commentRepository.existsByIdAndUserId(commentId, userId);
    }
}