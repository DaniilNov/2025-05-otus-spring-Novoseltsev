package ru.otus.hw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.resilience")
public class ResilienceProps {
    private int executorPoolSize = 16;
}