package ru.otus.hw.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.models.mongo.Author;
import ru.otus.hw.models.mongo.Book;
import ru.otus.hw.models.mongo.Comment;
import ru.otus.hw.models.mongo.Genre;

import java.util.ArrayList;
import java.util.List;

@ChangeLog(order = "001")
@Slf4j
public class InitMongoDBDataChangeLog {

    @ChangeSet(order = "001", id = "initAuthors", author = "otus-student")
    public void initAuthors(MongockTemplate mongockTemplate) {
        Author author1 = new Author(null, "Author_1");
        Author author2 = new Author(null, "Author_2");
        Author author3 = new Author(null, "Author_3");
        Author author4 = new Author(null, "Author_4");
        Author author5 = new Author(null, "Author_5");

        List<Author> authors = List.of(author1, author2, author3, author4, author5);
        for (Author author : authors) {
            mongockTemplate.save(author);
        }

        log.info("=== MONGOCK: Created {} authors ===", authors.size());
    }

    @ChangeSet(order = "002", id = "initGenres", author = "otus-student")
    public void initGenres(MongockTemplate mongockTemplate) {
        Genre genre1 = new Genre(null, "Genre_1");
        Genre genre2 = new Genre(null, "Genre_2");
        Genre genre3 = new Genre(null, "Genre_3");
        Genre genre4 = new Genre(null, "Genre_4");

        List<Genre> genres = List.of(genre1, genre2, genre3, genre4);
        for (Genre genre : genres) {
            mongockTemplate.save(genre);
        }

        log.info("=== MONGOCK: Created {} genres ===", genres.size());
    }

    @ChangeSet(order = "003", id = "initBooks", author = "otus-student")
    public void initBooks(MongockTemplate mongockTemplate) {
        List<Author> authors = mongockTemplate.findAll(Author.class);
        List<Genre> genres = mongockTemplate.findAll(Genre.class);

        if (authors.isEmpty() || genres.isEmpty()) {
            log.error("Cannot create books: authors={}, genres={}", authors.size(), genres.size());
            return;
        }

        List<Book> books = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Author randomAuthor = authors.get((i - 1) % authors.size());
            Genre randomGenre = genres.get((i - 1) % genres.size());
            Book book = new Book(null, "BookTitle_" + i, randomAuthor, randomGenre);
            books.add(book);
        }

        for (Book book : books) {
            mongockTemplate.save(book);
        }

        log.info("=== MONGOCK: Created {} books ===", books.size());

    }

    @ChangeSet(order = "004", id = "initComments", author = "otus-student")
    public void initComments(MongockTemplate mongockTemplate) {
        List<Book> books = mongockTemplate.findAll(Book.class);
        if (books.isEmpty()) {
            log.error("Cannot create comments: no books found");
            return;
        }

        String[] commentTexts = {"Great book!", "Could be better", "Not bad", "Excellent read", "Highly recommend"};
        int totalComments = 0;

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            int numComments = 1 + (i % 2);

            for (int j = 0; j < numComments; j++) {
                String commentText = commentTexts[(i + j) % commentTexts.length];
                Comment comment = new Comment(null, commentText, book);
                mongockTemplate.save(comment);
                totalComments++;
            }
        }
        log.info("=== MONGOCK: Created {} comments for {} books ===", totalComments, books.size());
        log.info("=== MONGOCK: MongoDB initialization completed! ===");
    }
}