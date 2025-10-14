package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.GenreProcessingWrapper;
import ru.otus.hw.models.jpa.Genre;

@Component
@RequiredArgsConstructor
public class GenreItemProcessor implements ItemProcessor<ru.otus.hw.models.mongo.Genre, GenreProcessingWrapper> {

    @Override
    public GenreProcessingWrapper process(ru.otus.hw.models.mongo.Genre mongoGenre) {
        Genre jpaGenre = new Genre();
        jpaGenre.setName(mongoGenre.getName());
        return new GenreProcessingWrapper(mongoGenre, jpaGenre);
    }
}