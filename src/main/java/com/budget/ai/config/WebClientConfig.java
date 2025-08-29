package com.budget.ai.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    @Qualifier("serviceWebClient")
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${external.service.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    @Qualifier("openAIWebClient")
    public WebClient openAIWebClient(WebClient.Builder builder,
                                     @Value("${external.openai.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
}