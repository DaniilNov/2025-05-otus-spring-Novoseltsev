package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN') or @commentServiceImpl.isOwnerByIdAndUsername(#id, authentication.name)")
    public Comment update(String id, String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setText(text);
        return commentRepository.save(comment);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN') or @commentServiceImpl.isOwnerByIdAndUsername(#id, authentication.name)")
    public void deleteById(String id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id %s not found".formatted(id));
        }
        commentRepository.deleteById(id);
    }

    public boolean isOwnerByIdAndUsername(String commentId, String username) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            User commentUser = comment.getUser();
            if (commentUser != null) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                return userOpt.isPresent() && userOpt.get().getId().equals(commentUser.getId());
            }
        }
        return false;
    }
}