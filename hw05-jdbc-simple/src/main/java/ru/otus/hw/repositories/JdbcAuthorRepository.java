package ru.otus.hw.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcAuthorRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Author> findAll() {
        return jdbcTemplate.query("SELECT id, full_name FROM authors", new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        var params = Map.of("id", id);
        return jdbcTemplate.query(
                "SELECT id, full_name FROM authors WHERE id = :id",
                params,
                new AuthorRowMapper()
        ).stream().findFirst();
    }

    private static class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Author(rs.getLong("id"), rs.getString("full_name"));
        }
    }

    @Override
    public List<Author> findAllById(Set<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        var params = Map.of("ids", ids);
        return jdbcTemplate.query(
                "SELECT id, full_name FROM authors WHERE id IN (:ids)",
                params,
                new AuthorRowMapper()
        );
    }
}
