package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.jpa.Genre;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreMappingService {

    private final EntityManager entityManager;

    private final ConcurrentHashMap<String, Genre> genreCache = new ConcurrentHashMap<>();

    @Transactional
    public Genre getOrCreateGenre(ru.otus.hw.models.mongo.Genre mongoGenre) {
        String genreName = mongoGenre.getName();

        Genre cachedGenre = genreCache.get(genreName);
        if (cachedGenre != null) {
            return cachedGenre;
        }

        Genre existingGenre = findGenreByName(genreName);
        if (existingGenre != null) {
            genreCache.put(genreName, existingGenre);
            return existingGenre;
        }

        Genre newGenre = new Genre();
        newGenre.setName(genreName);
        entityManager.persist(newGenre);
        entityManager.flush();

        genreCache.put(genreName, newGenre);
        log.debug("Created new genre: {}", genreName);

        return newGenre;
    }

    private Genre findGenreByName(String name) {
        TypedQuery<Genre> query = entityManager.createQuery(
                "SELECT g FROM Genre g WHERE g.name = :name", Genre.class);
        query.setParameter("name", name);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public void clearCache() {
        genreCache.clear();
    }
}