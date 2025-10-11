package ru.otus.hw.batch.processor;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.model.BookProcessingWrapper;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.service.TemporaryMappingService;

@Component
@RequiredArgsConstructor
public class BookItemProcessor implements ItemProcessor<ru.otus.hw.models.mongo.Book, BookProcessingWrapper> {

    private final EntityManager entityManager;

    private final TemporaryMappingService temporaryMappingService;

    @Override
    public BookProcessingWrapper process(ru.otus.hw.models.mongo.Book mongoBook) {
        String mongoAuthorId = mongoBook.getAuthor().getId();
        String mongoGenreId = mongoBook.getGenre().getId();

        Long authorJpaId = temporaryMappingService.getAuthorJpaIdByMongoId(mongoAuthorId);
        if (authorJpaId == null) {
            throw new IllegalStateException("Author JPA ID not found for MongoDB ID: " + mongoAuthorId);
        }

        Long genreJpaId = temporaryMappingService.getGenreJpaIdByMongoId(mongoGenreId);
        if (genreJpaId == null) {
            throw new IllegalStateException("Genre JPA ID not found for MongoDB ID: " + mongoGenreId);
        }

        Author authorRef = entityManager.getReference(Author.class, authorJpaId);
        Genre genreRef = entityManager.getReference(Genre.class, genreJpaId);

        Book bookJpa = new Book();
        bookJpa.setTitle(mongoBook.getTitle());
        bookJpa.setAuthor(authorRef);
        bookJpa.setGenre(genreRef);

        return new BookProcessingWrapper(mongoBook, bookJpa);
    }
}