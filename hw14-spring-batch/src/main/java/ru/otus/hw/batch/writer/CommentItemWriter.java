package ru.otus.hw.batch.writer;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Comment;

@Component
@RequiredArgsConstructor
public class CommentItemWriter implements ItemWriter<Comment> {

    private final EntityManager entityManager;

    @Override
    public void write(Chunk<? extends Comment> chunk) {
        chunk.getItems().forEach(entityManager::persist);
        entityManager.flush();
        entityManager.clear();
    }
}