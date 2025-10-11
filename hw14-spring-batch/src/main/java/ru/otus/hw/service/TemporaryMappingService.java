package ru.otus.hw.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TemporaryMappingService {

    private final Map<String, Long> authorMongoIdToJpaId = new ConcurrentHashMap<>();

    private final Map<String, Long> genreMongoIdToJpaId = new ConcurrentHashMap<>();

    private final Map<String, Long> bookMongoIdToJpaId = new ConcurrentHashMap<>();

    public void addAuthorMapping(String mongoId, Long jpaId) {
        authorMongoIdToJpaId.put(mongoId, jpaId);
    }

    public Long getAuthorJpaIdByMongoId(String mongoId) {
        return authorMongoIdToJpaId.get(mongoId);
    }

    public void addGenreMapping(String mongoId, Long jpaId) {
        genreMongoIdToJpaId.put(mongoId, jpaId);
    }

    public Long getGenreJpaIdByMongoId(String mongoId) {
        return genreMongoIdToJpaId.get(mongoId);
    }

    public void addBookMapping(String mongoId, Long jpaId) {
        bookMongoIdToJpaId.put(mongoId, jpaId);
    }

    public Long getBookJpaIdByMongoId(String mongoId) {
        return bookMongoIdToJpaId.get(mongoId);
    }

    public void clear() {
        authorMongoIdToJpaId.clear();
        genreMongoIdToJpaId.clear();
        bookMongoIdToJpaId.clear();
    }
}