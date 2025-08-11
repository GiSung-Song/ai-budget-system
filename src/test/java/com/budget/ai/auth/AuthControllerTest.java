package com.budget.ai.auth;

import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.testsupport.ControllerTest;
import com.budget.ai.testsupport.TestAuthHelper;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest
public class AuthControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.startRedis();

        TestContainerManager.registerMySQL(registry);
        TestContainerManager.registerRedis(registry);
    }

    @Nested
    @DisplayName("로그인 API 통합 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상 로그인")
        void 로그인_성공() throws Exception {
            // given
            testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            LoginRequest request = new LoginRequest("test@example.com", "password123");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").exists())
                    .andExpect(cookie().exists("refreshToken"))
                    .andDo(print());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void 로그인_존재하지않는이메일_예외() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void 로그인_잘못된비밀번호_예외() throws Exception {
            // given
            testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("필수 값 누락 시 예외 발생")
        void 로그인_필수값누락_예외() throws Exception {
            LoginRequest request = new LoginRequest(null, "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API 통합 테스트")
    class RefreshTokenTest {

        @Test
        @DisplayName("정상 토큰 재발급")
        void 토큰재발급_성공() throws Exception {
            // given - 사용자 생성 및 인증 설정
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "password123");

            redisTemplate.opsForValue().set("refresh:" + user.getId(), tokenResponse.refreshToken());

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refreshToken", tokenResponse.refreshToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                    .andDo(print());
        }

        @Test
        @DisplayName("쿠키가 없을 때 예외 발생")
        void 토큰재발급_쿠키없음_예외() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("유효하지 않은 refresh token으로 재발급 시 예외 발생")
        void 토큰재발급_유효하지않은토큰_예외() throws Exception {
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refreshToken", "invalid.token")))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("로그아웃 API 통합 테스트")
    class LogoutTest {

        @Test
        @DisplayName("정상 로그아웃")
        void 로그아웃_성공() throws Exception {
            // given - 사용자 생성 및 인증 설정
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "password123");

            // when & then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Set-Cookie", allOf(
                            containsString("refreshToken="),
                            containsString("Max-Age=0"),
                            containsString("Path=/"),
                            containsString("HttpOnly"),
                            containsString("Secure"))))
                    .andDo(print());
        }

        @Test
        @DisplayName("Authorization 헤더가 없을 때 예외 발생")
        void 로그아웃_헤더없음_예외() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("유효하지 않은 access token으로 로그아웃 시 예외 발생")
        void 로그아웃_유효하지않은토큰_예외() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer invalid.token"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("잘못된 Authorization 헤더 형식으로 로그아웃 시 예외 발생")
        void 로그아웃_잘못된헤더형식_예외() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "InvalidFormat invalid.token"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}
