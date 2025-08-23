package com.budget.ai.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

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
                .build();
    }
}