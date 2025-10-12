package ru.otus.hw.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Component("library")
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public Health health() {
        try {
            long authors = authorRepository.count();
            long genres = genreRepository.count();
            long books = bookRepository.count();
            boolean ok = authors > 0 && genres > 0 && books > 0;

            if (ok) {
                return Health.up()
                        .withDetail("authors", authors)
                        .withDetail("genres", genres)
                        .withDetail("books", books)
                        .build();
            } else {
                return Health.down()
                        .withDetail("authors", authors)
                        .withDetail("genres", genres)
                        .withDetail("books", books)
                        .withDetail("reason", "Empty essential collections detected")
                        .build();
            }
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}