package com.budget.ai.testsupport;

import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.auth.dto.response.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuthHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public TestAuthHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public TokenResponse setLogin(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        String accessToken = JsonPath.read(content, "$.data.accessToken");

        Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie.getValue() : null;

        return new TokenResponse(accessToken, refreshToken);
    }
}
