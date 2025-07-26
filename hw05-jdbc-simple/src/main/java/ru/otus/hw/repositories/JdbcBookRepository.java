package ru.otus.hw.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JdbcBookRepository implements BookRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;


    @Autowired
    public JdbcBookRepository(NamedParameterJdbcTemplate jdbcTemplate,
                              AuthorRepository authorRepository,
                              GenreRepository genreRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public Optional<Book> findById(long id) {
        var params = Map.of("id", id);
        Optional<Book> bookOptional = jdbcTemplate.query(
                "SELECT id, title, author_id, genre_id FROM books WHERE id = :id",
                params,
                new BookRowMapper()
        ).stream().findFirst();

        bookOptional.ifPresent(book -> {
            Author author = authorRepository.findById(book.getAuthor().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));
            book.setAuthor(author);

            Genre genre = genreRepository.findById(book.getGenre().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Genre not found"));
            book.setGenre(genre);
        });

        return bookOptional;
    }

    @Override
    public List<Book> findAll() {
        var books = jdbcTemplate.query(
                "SELECT id, title, author_id, genre_id FROM books",
                new BookRowMapper()
        );

        var authorIds = books.stream().map(b -> b.getAuthor().getId()).collect(Collectors.toSet());
        var genreIds = books.stream().map(b -> b.getGenre().getId()).collect(Collectors.toSet());

        var authors = authorRepository.findAllById(authorIds);
        var genres = genreRepository.findAllById(genreIds);

        books.forEach(book -> {
            authors.stream()
                    .filter(a -> a.getId() == book.getAuthor().getId())
                    .findFirst()
                    .ifPresent(book::setAuthor);

            genres.stream()
                    .filter(g -> g.getId() == book.getGenre().getId())
                    .findFirst()
                    .ifPresent(book::setGenre);
        });
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        var params = Map.of("id", id);
        jdbcTemplate.update("DELETE FROM books WHERE id = :id", params);
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource();
        params.addValue("title", book.getTitle());
        params.addValue("author_id", book.getAuthor().getId());
        params.addValue("genre_id", book.getGenre().getId());

        jdbcTemplate.update(
                "INSERT INTO books (title, author_id, genre_id) VALUES (:title, :author_id, :genre_id)",
                params,
                keyHolder,
                new String[]{"id"}
        );

        book.setId(keyHolder.getKeyAs(Long.class));
        return book;
    }

    private Book update(Book book) {
        var params = Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "author_id", book.getAuthor().getId(),
                "genre_id", book.getGenre().getId()
        );

        int updated = jdbcTemplate.update(
                "UPDATE books SET title = :title, author_id = :author_id, genre_id = :genre_id WHERE id = :id",
                params
        );

        if (updated == 0) {
            throw new EntityNotFoundException("No book with id = " + book.getId());
        }

        return book;
    }

    private static class BookRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            var author = new Author();
            author.setId(rs.getLong("author_id"));

            var genre = new Genre();
            genre.setId(rs.getLong("genre_id"));

            return new Book(
                    rs.getLong("id"),
                    rs.getString("title"),
                    author,
                    genre
            );
        }
    }
}
