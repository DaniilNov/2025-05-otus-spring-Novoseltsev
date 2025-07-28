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

@Repository
public class JdbcBookRepository implements BookRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcBookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Book> findById(long id) {
        var params = Map.of("id", id);
        return jdbcTemplate.query(
                "SELECT b.id, b.title, " +
                        "a.id as author_id, a.full_name as author_name, " +
                        "g.id as genre_id, g.name as genre_name " +
                        "FROM books b " +
                        "JOIN authors a ON b.author_id = a.id " +
                        "JOIN genres g ON b.genre_id = g.id " +
                        "WHERE b.id = :id",
                params,
                new BookRowMapper()
        ).stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        return jdbcTemplate.query(
                "SELECT b.id, b.title, " +
                        "a.id as author_id, a.full_name as author_name, " +
                        "g.id as genre_id, g.name as genre_name " +
                        "FROM books b " +
                        "JOIN authors a ON b.author_id = a.id " +
                        "JOIN genres g ON b.genre_id = g.id",
                new BookRowMapper()
        );
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
            var author = new Author(
                    rs.getLong("author_id"),
                    rs.getString("author_name")
            );

            var genre = new Genre(
                    rs.getLong("genre_id"),
                    rs.getString("genre_name")
            );

            return new Book(
                    rs.getLong("id"),
                    rs.getString("title"),
                    author,
                    genre
            );
        }
    }
}
