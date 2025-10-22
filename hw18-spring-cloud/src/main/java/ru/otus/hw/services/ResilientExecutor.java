package ru.otus.hw.services;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ResilientExecutor {

    private final RateLimiter mongoRateLimiter;

    private final TimeLimiter mongoTimeLimiter;

    private final ScheduledExecutorService resilienceScheduler;

    public <T> T execute(Supplier<T> supplier) {
        Supplier<T> rateLimited = io.github.resilience4j.ratelimiter.RateLimiter
                .decorateSupplier(mongoRateLimiter, supplier);
        try {
            return mongoTimeLimiter.executeFutureSupplier(
                    () -> CompletableFuture.supplyAsync(rateLimited::get, resilienceScheduler)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void executeVoid(Runnable runnable) {
        Runnable rateLimited = io.github.resilience4j.ratelimiter.RateLimiter
                .decorateRunnable(mongoRateLimiter, runnable);
        try {
            mongoTimeLimiter.executeFutureSupplier(
                    () -> CompletableFuture.runAsync(rateLimited, resilienceScheduler)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeOrFallback(Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return execute(supplier);
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof TimeoutException || cause instanceof RequestNotPermitted) {
                return fallback.get();
            }
            throw ex;
        }
    }

    public void executeVoidOrFallback(Runnable runnable, Runnable fallback) {
        try {
            executeVoid(runnable);
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof TimeoutException || cause instanceof RequestNotPermitted) {
                fallback.run();
                return;
            }
            throw ex;
        }
    }
}