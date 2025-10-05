package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.BookProcessingWrapper;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.service.AuthorMappingService;
import ru.otus.hw.service.GenreMappingService;

@Component
@RequiredArgsConstructor
public class BookItemProcessor implements ItemProcessor<ru.otus.hw.models.mongo.Book, BookProcessingWrapper> {

    private final AuthorMappingService authorMappingService;

    private final GenreMappingService genreMappingService;

    @Override
    public BookProcessingWrapper process(ru.otus.hw.models.mongo.Book mongoBook) {
        Author authorJpa = authorMappingService.getOrCreateAuthor(mongoBook.getAuthor());
        Genre genreJpa = genreMappingService.getOrCreateGenre(mongoBook.getGenre());

        Book bookJpa = new Book();
        bookJpa.setTitle(mongoBook.getTitle());
        bookJpa.setAuthor(authorJpa);
        bookJpa.setGenre(genreJpa);

        return new BookProcessingWrapper(mongoBook, bookJpa);
    }
}