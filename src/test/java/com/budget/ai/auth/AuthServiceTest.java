package com.budget.ai.auth;

import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.testsupport.ServiceTest;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ServiceTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TestDataFactory testDataFactory;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상 로그인")
        void 로그인_성공() {
            // given
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            LoginRequest request = new LoginRequest(user.getEmail(), "password123");

            // when
            TokenResponse response = authService.login(request);

            // then
            assertThat(response.accessToken()).isNotNull();
            assertThat(response.refreshToken()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void 로그인_존재하지않는이메일_예외() {
            LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_REQUEST);
                    });
        }

        @Test
        @DisplayName("회원 탈퇴한 이메일로 로그인 시 예외 발생")
        void 로그인_회원탈퇴한_회원_예외() {
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            user.softDelete();

            LoginRequest request = new LoginRequest("test@example.com", "password123");

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.DELETED_USER);
                    });
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void 로그인_잘못된비밀번호_예외() {
            // given
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            LoginRequest request = new LoginRequest(user.getEmail(), "wrongpassword");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_REQUEST);
                    });
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class RefreshTokenTest {

        @Test
        @DisplayName("정상 토큰 재발급")
        void 토큰재발급_성공() {
            // given
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            LoginRequest loginRequest = new LoginRequest(user.getEmail(), "password123");
            TokenResponse loginResponse = authService.login(loginRequest);

            // when
            TokenResponse refreshResponse = authService.refreshToken(loginResponse.refreshToken());

            // then
            assertThat(refreshResponse.accessToken()).isNotNull();
            assertThat(refreshResponse.refreshToken()).isNull(); // refreshToken은 null 반환
        }

        @Test
        @DisplayName("null refresh token으로 재발급 시 예외 발생")
        void 토큰재발급_null_토큰_예외() {
            assertThatThrownBy(() -> authService.refreshToken(null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }

        @Test
        @DisplayName("유효하지 않은 refresh token으로 재발급 시 예외 발생")
        void 토큰재발급_유효하지않은토큰_예외() {
            String invalidToken = "invalid.refresh.token";

            assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("정상 로그아웃")
        void 로그아웃_성공() {
            // given
            User user = testDataFactory.createUser("테스트사용자", "test@example.com", "password123");
            LoginRequest loginRequest = new LoginRequest(user.getEmail(), "password123");
            TokenResponse loginResponse = authService.login(loginRequest);

            // when
            authService.logout(loginResponse.accessToken());

            // then - 예외가 발생하지 않으면 성공
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 시 예외 발생")
        void 로그아웃_유효하지않은토큰_예외() {
            String invalidToken = "invalid.access.token";

            assertThatThrownBy(() -> authService.logout(invalidToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }

        @Test
        @DisplayName("null 토큰으로 로그아웃 시 예외 발생")
        void 로그아웃_null_토큰_예외() {
            assertThatThrownBy(() -> authService.logout(null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }
    }
}
