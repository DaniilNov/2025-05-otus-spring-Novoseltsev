package ru.otus.hw.services;

import ru.otus.hw.models.Comment;
import ru.otus.hw.models.User;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Optional<Comment> findById(String id);

    List<Comment> findByBookId(String bookId);

    List<Comment> findByUser(User user);

    Comment create(String text, String bookId, User author);

    Comment update(String id, String text, User user);

    void deleteById(String id, User user);

    boolean isOwner(String commentId, String userId);
}
