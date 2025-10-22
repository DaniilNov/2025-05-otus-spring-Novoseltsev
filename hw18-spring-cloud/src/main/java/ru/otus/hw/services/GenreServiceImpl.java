package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    private final ResilientExecutor resilientExecutor;

    @Override
    public List<Genre> findAll() {
        return resilientExecutor.executeOrFallback(genreRepository::findAll, Collections::emptyList);
    }
}