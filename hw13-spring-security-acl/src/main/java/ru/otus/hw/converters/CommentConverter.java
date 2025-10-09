package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Comment;

@Component
public class CommentConverter {
    public String commentToString(Comment comment) {
        if (comment == null) {
            return "Comment is null";
        }
        String userId = (comment.getUser() != null) ? comment.getUser().getId() : "unknown";
        return "Id: %s, Text: %s, BookId: %s, UserId: %s".formatted(
                comment.getId(),
                comment.getText(),
                comment.getBook() != null ? comment.getBook().getId() : "unknown",
                userId);
    }
}