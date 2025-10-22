package ru.otus.hw.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableConfigurationProperties(ResilienceProps.class)
public class ResilienceConfig {

    @Bean
    public RateLimiter mongoRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("mongoDbCalls");
    }

    @Bean
    public TimeLimiter mongoTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("mongoDbCalls");
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService resilienceScheduler(ResilienceProps props) {
        return Executors.newScheduledThreadPool(props.getExecutorPoolSize());
    }
}