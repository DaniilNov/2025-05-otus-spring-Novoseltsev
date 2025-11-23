package com.example.task.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class WorkflowValidationService {

    private final RestClient workflowRestClient;

    @Value("${workflow.service.url}")
    private String workflowServiceBaseUrl;

    public WorkflowValidationService(@Qualifier("workflowRestClient") RestClient workflowRestClient) {
        this.workflowRestClient = workflowRestClient;
    }

    @CircuitBreaker(name = "workflow-service", fallbackMethod = "isValidTransitionFallback")
    @Retry(name = "workflow-service")
    public boolean isValidTransition(Long workflowId, String fromStatus, String toStatus, String role) {
        String path = "/api/workflows/{id}/validate";

        try {
            log.info("Calling workflow service for validation:" +
                            " {}{} with params id={}, fromStatus={}, toStatus={}, role={}",
                    workflowServiceBaseUrl, path, workflowId, fromStatus, toStatus, role);

            Boolean result = workflowRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path(path)
                            .queryParam("fromStatus", fromStatus)
                            .queryParam("toStatus", toStatus)
                            .queryParam("role", role)
                            .build(workflowId))
                    .retrieve()
                    .body(Boolean.class);

            log.info("Workflow service response: {}", result);
            return result != null && result;
        } catch (Exception e) {
            log.error("Error calling workflow service: {}", e.getMessage());
            throw e;
        }
    }

    @CircuitBreaker(name = "workflow-service", fallbackMethod = "checkWorkflowExistsFallback")
    @Retry(name = "workflow-service")
    public boolean checkWorkflowExists(Long workflowId) {
        String path = "/api/workflows/{id}";

        try {
            log.info("Calling workflow service to check existence: {}{} with id={}",
                    workflowServiceBaseUrl, path, workflowId); // Используем workflowServiceBaseUrl

            Boolean exists = workflowRestClient.get()
                    .uri(path, workflowId)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode()
                    .is2xxSuccessful();

            log.info("Workflow existence check result for id {}: {}", workflowId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error calling workflow service to check workflow existence: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isValidTransitionFallback(Long workflowId, String fromStatus, String toStatus,
                                             String role, Exception ex) {
        log.warn("Circuit breaker OPEN or retries exhausted for workflow validation. Defaulting to false. Error: {}",
                ex.getMessage());
        return false;
    }

    public boolean checkWorkflowExistsFallback(Long workflowId, Exception ex) {
        log.warn("Circuit breaker OPEN or retries exhausted for workflow existence check. Defaulting to false. Error:" +
                " {}", ex.getMessage());
        return false;
    }

}