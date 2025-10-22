package ru.otus.hw.services;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Resilience4j: проверка RateLimiter и TimeLimiter")
class ResilientExecutorTest {

    @Test
    @DisplayName("RateLimiter: разрешает 3 из 6 вызовов за 1s, 3 — блокирует")
    void ratelimiter_allows_three_and_blocks_the_rest() {
        RateLimiter rateLimiter = RateLimiter.of(
                "mongoDbCalls",
                RateLimiterConfig.custom()
                        .limitForPeriod(3)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO)
                        .build()
        );

        int allowed = 0, blocked = 0;
        for (int i = 0; i < 6; i++) {
            if (rateLimiter.acquirePermission()) {
                allowed++;
            } else {
                blocked++;
            }
        }

        assertThat(allowed).isEqualTo(3);
        assertThat(blocked).isEqualTo(3);
    }

    @Test
    @DisplayName("TimeLimiter: выбрасывает TimeoutException при превышении таймаута 100 мс")
    void timelimiter_throws_timeout_exception_on_slow_task() {
        TimeLimiter timeLimiter = TimeLimiter.of(
                "mongoDbCalls",
                TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofMillis(100))
                        .cancelRunningFuture(true)
                        .build()
        );

        var pool = Executors.newFixedThreadPool(2);

        try {
            timeLimiter.executeFutureSupplier(() -> pool.submit(() -> {
                Thread.sleep(150);
                return "slow";
            }));
            throw new AssertionError("Ожидался TimeoutException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(TimeoutException.class);
        } finally {
            pool.shutdownNow();
        }
    }
}