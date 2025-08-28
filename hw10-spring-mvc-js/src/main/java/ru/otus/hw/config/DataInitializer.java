package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        try {
            long authorCount = mongoTemplate.getCollection("authors").estimatedDocumentCount();
            if (authorCount == 0) {
                log.info("Initializing sample data...");
                this.initializeData();
                log.info("Sample data successfully loaded.");
            } else {
                log.info("Data already exists, skipping initialization.");
            }
        } catch (Exception ex) {
            log.error("Error during data initialization", ex);
        }
        log.info("Data initialization finished.");
    }

    private void initializeData() {
        mongoTemplate.getDb().drop();
        log.info("Dropped existing collections.");

        List<Author> savedAuthors = this.createAndSaveAuthors();

        List<Genre> savedGenres = this.createAndSaveGenres();

        List<Book> savedBooks = this.createAndSaveBooks(savedAuthors, savedGenres);

        this.createAndSaveComments(savedBooks);
    }

    private List<Author> createAndSaveAuthors() {
        Author author1 = new Author(null, "Author_1");
        Author author2 = new Author(null, "Author_2");
        Author author3 = new Author(null, "Author_3");
        List<Author> savedAuthors = authorRepository.saveAll(List.of(author1, author2, author3));
        log.debug("Saved {} authors", savedAuthors.size());
        return savedAuthors;
    }

    private List<Genre> createAndSaveGenres() {
        Genre genre1 = new Genre(null, "Genre_1");
        Genre genre2 = new Genre(null, "Genre_2");
        Genre genre3 = new Genre(null, "Genre_3");
        List<Genre> savedGenres = genreRepository.saveAll(List.of(genre1, genre2, genre3));
        log.debug("Saved {} genres", savedGenres.size());
        return savedGenres;
    }

    private List<Book> createAndSaveBooks(List<Author> authors, List<Genre> genres) {
        Book book1 = new Book(null, "BookTitle_1", authors.get(0), genres.get(0));
        Book book2 = new Book(null, "BookTitle_2", authors.get(1), genres.get(1));
        Book book3 = new Book(null, "BookTitle_3", authors.get(2), genres.get(2));
        List<Book> savedBooks = bookRepository.saveAll(List.of(book1, book2, book3));
        log.debug("Saved {} books", savedBooks.size());
        return savedBooks;
    }

    private void createAndSaveComments(List<Book> books) {
        Comment comment1 = new Comment(null, "Great book!", books.get(0));
        Comment comment2 = new Comment(null, "Could be better", books.get(1));
        Comment comment3 = new Comment(null, "Not bad", books.get(2));
        commentRepository.saveAll(List.of(comment1, comment2, comment3));
        log.debug("Saved {} comments", 3);
    }
}