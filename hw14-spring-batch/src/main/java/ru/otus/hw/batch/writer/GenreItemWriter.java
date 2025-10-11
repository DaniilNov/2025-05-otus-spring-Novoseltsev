package ru.otus.hw.batch.writer;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.GenreProcessingWrapper;
import ru.otus.hw.service.TemporaryMappingService;

@Component
@RequiredArgsConstructor
public class GenreItemWriter implements ItemWriter<GenreProcessingWrapper> {

    private final EntityManager entityManager;

    private final TemporaryMappingService temporaryMappingService;

    @Override
    public void write(Chunk<? extends GenreProcessingWrapper> chunk) {
        for (GenreProcessingWrapper wrapper : chunk) {
            entityManager.persist(wrapper.getJpaGenre());
        }
        entityManager.flush();

        for (GenreProcessingWrapper wrapper : chunk) {
            temporaryMappingService.addGenreMapping(
                    wrapper.getMongoGenre().getId(),
                    wrapper.getJpaGenre().getId()
            );
        }

        entityManager.clear();
    }
}