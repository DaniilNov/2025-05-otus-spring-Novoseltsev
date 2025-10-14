package ru.otus.hw.batch.writer;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.BookProcessingWrapper;
import ru.otus.hw.service.TemporaryMappingService;

@Component
@RequiredArgsConstructor
public class BookItemWriter implements ItemWriter<BookProcessingWrapper> {

    private final EntityManager entityManager;

    private final TemporaryMappingService temporaryMappingService;

    @Override
    public void write(Chunk<? extends BookProcessingWrapper> chunk) {
        for (BookProcessingWrapper wrapper : chunk) {
            entityManager.persist(wrapper.getJpaBook());
        }
        entityManager.flush();

        for (BookProcessingWrapper wrapper : chunk) {
            temporaryMappingService.addBookMapping(
                    wrapper.getMongoBook().getId(),
                    wrapper.getJpaBook().getId()
            );
        }

        entityManager.clear();
    }
}