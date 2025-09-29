package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Comment;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByBookId(String bookId);

    List<Comment> findByUserId(String userId);

    void deleteByBookId(String bookId);

    boolean existsByIdAndUserId(String commentId, String userId);
}
