package com.example.task.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class TokenValidationService {

    private final RestClient authRestClient;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public TokenValidationService(@Qualifier("authRestClient") RestClient authRestClient) {
        this.authRestClient = authRestClient;
    }

    @CircuitBreaker(name = "auth-service", fallbackMethod = "validateAndExtractTokenInfoFallback")
    @Retry(name = "auth-service")
    public Map<String, Object> validateAndExtractTokenInfo(String token) {
        String path = "/api/auth/validate";

        try {
            log.info("Sending validation request to: {} with token (masked)", authServiceUrl);

            log.debug("About to call retrieve().body() in TokenValidationService");

            Map<String, Object> response = authRestClient.get()
                    .uri(path)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            log.debug("Received response from auth-service: {}", response);

            if (response != null) {
                log.debug("Token validation response from auth-service: {}", response);
                if (response.containsKey("userId") && response.containsKey("role")) {
                    log.info("Token validated successfully for user.");
                    return response;
                } else {
                    log.warn("Token validation response missing userId or role: {}", response);
                    return null;
                }
            } else {
                log.warn("Token validation response body is null.");
                return null;
            }
        } catch (Exception e) {
            log.error("Error during token validation: {}", e.getMessage(), e); // Добавим стектрейс
            throw e;
        }
    }

    public Map<String, Object> validateAndExtractTokenInfoFallback(String token, Exception ex) {
        log.warn("Circuit breaker OPEN or retries exhausted for auth service validation. Returning null. Error: {}",
                ex.getMessage());
        return null;
    }
}