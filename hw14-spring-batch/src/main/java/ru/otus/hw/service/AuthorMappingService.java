package ru.otus.hw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.jpa.Author;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthorMappingService {

    private final EntityManager entityManager;
    private final ConcurrentHashMap<String, Author> authorCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public void loadAuthorsToCache() {
        List<Author> existingAuthors = entityManager
                .createQuery("SELECT a FROM Author a", Author.class)
                .getResultList();

        authorCache.clear();
        existingAuthors.forEach(author ->
                authorCache.put(author.getFullName(), author));
    }

    public Author getOrCreateAuthor(ru.otus.hw.models.mongo.Author mongoAuthor) {
        String authorName = mongoAuthor.getFullName();
        Author author = authorCache.get(authorName);

        if (author == null) {
            throw new IllegalStateException("Author not found in cache: " + authorName +
                    ". Make sure preload was executed.");
        }

        return author;
    }

}