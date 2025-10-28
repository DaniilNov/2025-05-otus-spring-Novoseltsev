package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@SpringBootTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.mongoDbCalls.sliding-window-type=COUNT_BASED",
        "resilience4j.circuitbreaker.instances.mongoDbCalls.sliding-window-size=2",
        "resilience4j.circuitbreaker.instances.mongoDbCalls.minimum-number-of-calls=2",
        "resilience4j.circuitbreaker.instances.mongoDbCalls.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.mongoDbCalls.wait-duration-in-open-state=5s",
        "resilience4j.circuitbreaker.instances.mongoDbCalls.permitted-number-of-calls-in-half-open-state=1",
        "resilience4j.ratelimiter.instances.mongoDbCalls.limitForPeriod=100",
        "resilience4j.ratelimiter.instances.mongoDbCalls.limitRefreshPeriod=1s",
        "resilience4j.ratelimiter.instances.mongoDbCalls.timeoutDuration=0s"
})
@DisplayName("Интеграционные тесты устойчивости BookService (CircuitBreaker)")
class BookServiceCircuitBreakerIntegrationTest {

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @Test
    @DisplayName("должен открывать CircuitBreaker после последовательных ошибок и не вызывать репозиторий, используя fallback")
    void shouldOpenCircuitBreakerAndUseFallbackWithoutHittingRepositoryAfterFailures() {
        when(bookRepository.findAll())
                .thenThrow(new RuntimeException("DB down"))
                .thenThrow(new RuntimeException("DB still down"))
                .thenReturn(List.of(new Book()));

        assertThatThrownBy(() -> bookService.findAll())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("temporarily unavailable");

        assertThatThrownBy(() -> bookService.findAll())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("temporarily unavailable");

        assertThatThrownBy(() -> bookService.findAll())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("temporarily unavailable");

        verify(bookRepository, times(2)).findAll();
        verifyNoMoreInteractions(bookRepository);
    }
}