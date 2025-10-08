package ru.otus.hw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Genre;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PreloadService {

    private final MongoTemplate mongoTemplate;
    private final EntityManager entityManager;

    public void preloadAuthorsAndGenres() {
        preloadAuthors();
        preloadGenres();
    }

    private void preloadAuthors() {
        List<ru.otus.hw.models.mongo.Author> mongoAuthors = mongoTemplate.findAll(ru.otus.hw.models.mongo.Author.class);

        for (ru.otus.hw.models.mongo.Author mongoAuthor : mongoAuthors) {
            Author existingAuthor = findAuthorByName(mongoAuthor.getFullName());
            if (existingAuthor == null) {
                Author newAuthor = new Author();
                newAuthor.setFullName(mongoAuthor.getFullName());
                entityManager.persist(newAuthor);
            }
        }
        entityManager.flush();
    }

    private void preloadGenres() {
        List<ru.otus.hw.models.mongo.Genre> mongoGenres = mongoTemplate.findAll(ru.otus.hw.models.mongo.Genre.class);

        for (ru.otus.hw.models.mongo.Genre mongoGenre : mongoGenres) {
            Genre existingGenre = findGenreByName(mongoGenre.getName());
            if (existingGenre == null) {
                Genre newGenre = new Genre();
                newGenre.setName(mongoGenre.getName());
                entityManager.persist(newGenre);
            }
        }
        entityManager.flush();
    }

    private Author findAuthorByName(String fullName) {
        try {
            return entityManager.createQuery("SELECT a FROM Author a WHERE a.fullName = :fullName", Author.class)
                    .setParameter("fullName", fullName)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private Genre findGenreByName(String name) {
        try {
            return entityManager.createQuery("SELECT g FROM Genre g WHERE g.name = :name", Genre.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}