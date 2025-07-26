package ru.otus.hw.repositories;

import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository {
    List<Genre> findAll();

    Optional<Genre> findById(long id);

    List<Genre> findAllById(Set<Long> ids);
}
