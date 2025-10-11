package ru.otus.hw.batch.writer;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.AuthorProcessingWrapper;
import ru.otus.hw.service.TemporaryMappingService;

@Component
@RequiredArgsConstructor
public class AuthorItemWriter implements ItemWriter<AuthorProcessingWrapper> {

    private final EntityManager entityManager;

    private final TemporaryMappingService temporaryMappingService;

    @Override
    public void write(Chunk<? extends AuthorProcessingWrapper> chunk) {
        for (AuthorProcessingWrapper wrapper : chunk) {
            entityManager.persist(wrapper.getJpaAuthor());
        }
        entityManager.flush();

        for (AuthorProcessingWrapper wrapper : chunk) {
            temporaryMappingService.addAuthorMapping(
                    wrapper.getMongoAuthor().getId(),
                    wrapper.getJpaAuthor().getId()
            );
        }

        entityManager.clear();
    }
}