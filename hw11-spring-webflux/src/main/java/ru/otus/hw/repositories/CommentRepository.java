package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.projections.CommentProjection;

public interface CommentRepository extends ReactiveMongoRepository<CommentProjection, String>, CommentCustomRepository {
    Flux<CommentProjection> findByBookId(String bookId);

    Mono<Void> deleteByBookId(String bookId);
}
