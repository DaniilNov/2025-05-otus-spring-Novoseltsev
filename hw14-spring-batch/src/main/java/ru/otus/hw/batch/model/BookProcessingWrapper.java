package ru.otus.hw.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class BookProcessingWrapper {
    private ru.otus.hw.models.mongo.Book mongoBook;

    private ru.otus.hw.models.jpa.Book jpaBook;
}