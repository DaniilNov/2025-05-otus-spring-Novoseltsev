package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.AuthorProcessingWrapper;
import ru.otus.hw.models.jpa.Author;

@Component
@RequiredArgsConstructor
public class AuthorItemProcessor implements ItemProcessor<ru.otus.hw.models.mongo.Author, AuthorProcessingWrapper> {

    @Override
    public AuthorProcessingWrapper process(ru.otus.hw.models.mongo.Author mongoAuthor) {
        Author jpaAuthor = new Author();
        jpaAuthor.setFullName(mongoAuthor.getFullName());
        return new AuthorProcessingWrapper(mongoAuthor, jpaAuthor);
    }
}