package ru.otus.hw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.jpa.Genre;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GenreMappingService {

    private final EntityManager entityManager;
    private final ConcurrentHashMap<String, Genre> genreCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public void loadGenresToCache() {
        List<Genre> existingGenres = entityManager
                .createQuery("SELECT g FROM Genre g", Genre.class)
                .getResultList();

        genreCache.clear();
        existingGenres.forEach(genre ->
                genreCache.put(genre.getName(), genre));
    }

    public Genre getOrCreateGenre(ru.otus.hw.models.mongo.Genre mongoGenre) {
        String genreName = mongoGenre.getName();
        Genre genre = genreCache.get(genreName);

        if (genre == null) {
            throw new IllegalStateException("Genre not found in cache: " + genreName +
                    ". Make sure preload was executed.");
        }

        return genre;
    }

}