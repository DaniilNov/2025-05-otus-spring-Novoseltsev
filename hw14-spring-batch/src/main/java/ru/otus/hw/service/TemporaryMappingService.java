package ru.otus.hw.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TemporaryMappingService {

    private final Map<String, Long> bookMongoIdToJpaId = new ConcurrentHashMap<>();

    public void addBookMapping(String mongoId, Long jpaId) {
        bookMongoIdToJpaId.put(mongoId, jpaId);
    }

    public Long getJpaIdByMongoId(String mongoId) {
        return bookMongoIdToJpaId.get(mongoId);
    }

    public void clear() {
        bookMongoIdToJpaId.clear();
    }
}