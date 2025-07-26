package ru.otus.hw.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcGenreRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres", new GenreRowMapper());
    }

    @Override
    public Optional<Genre> findById(long id) {
        var params = Map.of("id", id);
        return jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id = :id",
                params,
                new GenreRowMapper()
        ).stream().findFirst();
    }

    private static class GenreRowMapper implements RowMapper<Genre> {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Genre(rs.getLong("id"), rs.getString("name"));
        }
    }

    @Override
    public List<Genre> findAllById(Set<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        var params = Map.of("ids", ids);
        return jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id IN (:ids)",
                params,
                new GenreRowMapper()
        );
    }
}
