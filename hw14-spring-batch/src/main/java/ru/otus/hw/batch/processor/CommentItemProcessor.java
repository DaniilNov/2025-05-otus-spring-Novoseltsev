package ru.otus.hw.batch.processor;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.service.TemporaryMappingService;

@Component
@RequiredArgsConstructor
public class CommentItemProcessor implements ItemProcessor<ru.otus.hw.models.mongo.Comment, Comment> {

    private final EntityManager entityManager;

    private final TemporaryMappingService temporaryMappingService;

    @Override
    public Comment process(ru.otus.hw.models.mongo.Comment mongoComment) {
        String mongoBookId = mongoComment.getBook().getId();
        Long bookJpaId = temporaryMappingService.getJpaIdByMongoId(mongoBookId);

        if (bookJpaId == null) {
            throw new IllegalStateException("Book JPA ID not found for MongoDB ID: " + mongoBookId);
        }

        Book book = entityManager.getReference(Book.class, bookJpaId);

        Comment jpaComment = new Comment();
        jpaComment.setText(mongoComment.getText());
        jpaComment.setBook(book);

        return jpaComment;
    }
}