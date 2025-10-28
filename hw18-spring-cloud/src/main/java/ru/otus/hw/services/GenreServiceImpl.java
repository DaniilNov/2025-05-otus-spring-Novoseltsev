package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @CircuitBreaker(name = "mongoDbCalls", fallbackMethod = "findAllFallback")
    @RateLimiter(name = "mongoDbCalls")
    @Override
    public List<Genre> findAll() {
        log.debug("Finding all genres");
        return genreRepository.findAll();
    }

    public List<Genre> findAllFallback(Throwable t) {
        log.error("Fallback for findAll genres: {}", t.toString());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Genre list is temporarily unavailable. Please try again later.",
                t
        );
    }
}