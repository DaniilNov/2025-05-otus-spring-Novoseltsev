package ru.otus.hw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.jpa.Author;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthorMappingService {

    private final EntityManager entityManager;

    private final ConcurrentHashMap<String, Author> authorCache = new ConcurrentHashMap<>();

    @Transactional
    public Author getOrCreateAuthor(ru.otus.hw.models.mongo.Author mongoAuthor) {
        String authorName = mongoAuthor.getFullName();

        return authorCache.computeIfAbsent(authorName, name -> {
            Author existing = findAuthorByName(name);
            if (existing != null) {
                return existing;
            }

            Author newAuthor = new Author();
            newAuthor.setFullName(name);
            entityManager.persist(newAuthor);
            entityManager.flush();
            return newAuthor;
        });
    }

    private Author findAuthorByName(String fullName) {
        try {
            return entityManager.createQuery(
                            "SELECT a FROM Author a WHERE a.fullName = :fullName", Author.class)
                    .setParameter("fullName", fullName)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public void clearCache() {
        authorCache.clear();
    }
}