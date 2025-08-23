package com.budget.ai.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
public class TestAuthHelperConfig {

    @Bean
    public TestAuthHelper testAuthHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        return new TestAuthHelper(mockMvc, objectMapper);
    }
}
