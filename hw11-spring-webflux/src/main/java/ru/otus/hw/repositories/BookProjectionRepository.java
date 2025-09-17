package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import ru.otus.hw.projections.BookProjection;

public interface BookProjectionRepository extends ReactiveMongoRepository<BookProjection, String> {
}