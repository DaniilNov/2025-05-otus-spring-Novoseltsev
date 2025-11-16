package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "findAllFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public List<Author> findAll() {
        log.debug("Finding all authors");
        return authorRepository.findAll();
    }

    public List<Author> findAllFallback(Throwable t) {
        log.error("Fallback for findAll authors: {}", t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Author list is temporarily unavailable. Please try again later.",
                t
        );
    }
}