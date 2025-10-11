package ru.otus.hw.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.hw.models.jpa.Author;

@Data
@AllArgsConstructor
public class AuthorProcessingWrapper {
    private ru.otus.hw.models.mongo.Author mongoAuthor;

    private Author jpaAuthor;
}