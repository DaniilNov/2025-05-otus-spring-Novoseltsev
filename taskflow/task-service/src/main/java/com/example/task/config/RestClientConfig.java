package com.example.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${workflow.service.url}")
    private String workflowServiceUrl;

    @Bean
    public RestClient authRestClient() {
        return RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    @Bean
    public RestClient workflowRestClient() {
        return RestClient.builder()
                .baseUrl(workflowServiceUrl)
                .build();
    }
}