package ru.otus.hw.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.hw.models.jpa.Genre;

@Data
@AllArgsConstructor
public class GenreProcessingWrapper {
    private ru.otus.hw.models.mongo.Genre mongoGenre;

    private Genre jpaGenre;
}