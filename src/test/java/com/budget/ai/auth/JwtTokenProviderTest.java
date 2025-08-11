package com.budget.ai.auth;

import com.budget.ai.auth.dto.request.JwtPayload;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secretKey = "sdapifjpi324jpifqhidpashf803h280i1fhidshaf80h340281q";
        Long accessExpiration = 1000 * 60 * 1L;
        Long refreshExpiration = 1000 * 60 * 60L;

        jwtTokenProvider = new JwtTokenProvider(secretKey, accessExpiration, refreshExpiration);
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class TokenGenerationTest {

        @Test
        @DisplayName("액세스 토큰 생성 및 파싱 성공")
        void 액세스_토큰_생성_및_파싱_성공() {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");

            String accessToken = jwtTokenProvider.generateAccessToken(jwtPayload);
            JwtPayload parsedToken = jwtTokenProvider.parseAccessToken(accessToken);

            assertThat(parsedToken.id()).isEqualTo(jwtPayload.id());
            assertThat(parsedToken.name()).isEqualTo(jwtPayload.name());
        }

        @Test
        @DisplayName("리프레시 토큰 생성 및 파싱 성공")
        void 리프레시_토큰_생성_및_파싱_성공() {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");

            String refreshToken = jwtTokenProvider.generateRefreshToken(jwtPayload);
            Long userId = jwtTokenProvider.parseRefreshToken(refreshToken);

            assertThat(userId).isEqualTo(jwtPayload.id());
        }

        @Test
        @DisplayName("JwtPayload가 null일 때 예외 발생")
        void 토큰생성_JwtPayload_null_예외() {
            assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.MISSING_JWT_PAYLOAD);
                    });
        }

        @Test
        @DisplayName("JwtPayload의 id가 null일 때 예외 발생")
        void 토큰생성_id_null_예외() {
            JwtPayload jwtPayload = new JwtPayload(null, "테스터", "test@email.com");

            assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(jwtPayload))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.MISSING_JWT_PAYLOAD);
                    });
        }

        @Test
        @DisplayName("JwtPayload의 name이 null일 때 예외 발생")
        void 토큰생성_name_null_예외() {
            JwtPayload jwtPayload = new JwtPayload(1L, null, "test@email.com");
            assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(jwtPayload))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.MISSING_JWT_PAYLOAD);
                    });
        }

        @Test
        @DisplayName("JwtPayload의 email이 null일 때 예외 발생")
        void 토큰생성_name_빈문자열_예외() {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", null);
            assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(jwtPayload))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.MISSING_JWT_PAYLOAD);
                    });
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTest {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void 토큰검증_성공() {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");
            String token = jwtTokenProvider.generateAccessToken(jwtPayload);

            boolean isValid = jwtTokenProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 토큰 검증 실패")
        void 토큰검증_잘못된토큰_실패() {
            String invalidToken = "invalid.token.here";

            boolean isValid = jwtTokenProvider.validateToken(invalidToken);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("null 토큰 검증 실패")
        void 토큰검증_null_실패() {
            boolean isValid = jwtTokenProvider.validateToken(null);

            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 파싱 테스트")
    class TokenParsingTest {

        @Test
        @DisplayName("정상 Access Token 파싱")
        void 액세스토큰_파싱_성공() {
            JwtPayload originalPayload = new JwtPayload(1L, "테스터", "test@email.com");
            String accessToken = jwtTokenProvider.generateAccessToken(originalPayload);

            JwtPayload parsedPayload = jwtTokenProvider.parseAccessToken(accessToken);

            assertThat(parsedPayload.id()).isEqualTo(originalPayload.id());
            assertThat(parsedPayload.name()).isEqualTo(originalPayload.name());
            assertThat(parsedPayload.email()).isEqualTo(originalPayload.email());
        }

        @Test
        @DisplayName("정상 Refresh Token 파싱")
        void 리프레시토큰_파싱_성공() {
            JwtPayload originalPayload = new JwtPayload(1L, "테스터", "test@email.com");
            String refreshToken = jwtTokenProvider.generateRefreshToken(originalPayload);

            Long userId = jwtTokenProvider.parseRefreshToken(refreshToken);

            assertThat(userId).isEqualTo(originalPayload.id());
        }

        @Test
        @DisplayName("잘못된 Access Token 파싱 시 예외 발생")
        void 액세스토큰_파싱_잘못된토큰_예외() {
            String invalidToken = "invalid.token.here";

            assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(invalidToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }

        @Test
        @DisplayName("잘못된 Refresh Token 파싱 시 예외 발생")
        void 리프레시토큰_파싱_잘못된토큰_예외() {
            String invalidToken = "invalid.token.here";

            assertThatThrownBy(() -> jwtTokenProvider.parseRefreshToken(invalidToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(exception -> {
                        CustomException customException = (CustomException) exception;
                        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }
    }

    @Nested
    @DisplayName("토큰 만료 테스트")
    class TokenExpirationTest {

        @Test
        @DisplayName("토큰 만료 시간 조회")
        void 토큰만료시간_조회() {
            JwtPayload payload = new JwtPayload(1L, "테스터", "test@email.com");
            String token = jwtTokenProvider.generateAccessToken(payload);

            java.util.Date expiration = jwtTokenProvider.getTokenExpiration(token);

            assertThat(expiration).isNotNull();
            assertThat(expiration.after(new java.util.Date())).isTrue();
        }

        @Test
        @DisplayName("토큰 만료 여부 확인 - 만료되지 않은 토큰")
        void 토큰만료여부_만료되지않음() {
            JwtPayload payload = new JwtPayload(1L, "테스터", "test@email.com");
            String token = jwtTokenProvider.generateAccessToken(payload);

            boolean isExpired = jwtTokenProvider.isTokenExpired(token);

            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("토큰 만료 여부 확인 - 잘못된 토큰")
        void 토큰만료여부_잘못된토큰() {
            String invalidToken = "invalid.token.here";

            boolean isExpired = jwtTokenProvider.isTokenExpired(invalidToken);

            assertThat(isExpired).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰 파싱할 시 401 반환")
        void 만료된_토큰_파싱할_시_401_반환() throws InterruptedException {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");

            JwtTokenProvider shortJwtProvider = new JwtTokenProvider(
                    "fdsaoifjdoaifjajrui3ej2091j390fdj09wafj0pdas", 1L, 1L);

            String shortAccessToken = shortJwtProvider.generateAccessToken(jwtPayload);

            Thread.sleep(10);

            assertThatThrownBy(() -> shortJwtProvider.parseAccessToken(shortAccessToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("만료된 토큰 여부 확인할 시 만료되었으면 true 반환")
        void 만료된_토큰_여부_확인할_시_만료되었으면_true_반환() throws InterruptedException {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");

            JwtTokenProvider shortJwtProvider = new JwtTokenProvider(
                    "fdsaoifjdoaifjajrui3ej2091j390fdj09wafj0pdas", 1L, 1L);

            String shortAccessToken = shortJwtProvider.generateAccessToken(jwtPayload);

            Thread.sleep(10);

            assertThat(jwtTokenProvider.isTokenExpired(shortAccessToken)).isTrue();
        }
    }

    @Nested
    @DisplayName("토큰 해시 변환 테스트")
    class TokenHashTest {

        @Test
        @DisplayName("정상 토큰 해시 변환")
        void 토큰해시변환_성공() {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");
            String accessToken = jwtTokenProvider.generateAccessToken(jwtPayload);

            Optional<String> hash = jwtTokenProvider.tokenToHash(accessToken);

            assertThat(hash).isPresent();
            assertThat(hash.get()).isNotEmpty();
            assertThat(hash.get()).hasSize(64); // SHA-256 해시는 64자
        }

        @Test
        @DisplayName("null 토큰 해시 변환")
        void 토큰해시변환_null() {
            Optional<String> hash = jwtTokenProvider.tokenToHash(null);

            assertThat(hash).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 토큰 해시 변환")
        void 토큰해시변환_빈문자열() {
            Optional<String> hash = jwtTokenProvider.tokenToHash("");

            assertThat(hash).isPresent();
            assertThat(hash.get()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("기타 테스트")
    class OtherTests {

        @Test
        @DisplayName("유효하지 않은 토큰 파싱할 시 401 반환")
        void 유효하지_않은_토큰_파싱할_시_401_반환() {
            String invalidToken = "jwt.invalid.token";

            assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(invalidToken))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("토큰 검사할 시 정상 토큰이면 true 반환")
        void 토큰_검사할_시_정상_토큰이면_true_반환() {
            JwtPayload jwtPayload = new JwtPayload(1L, "테스터", "test@email.com");
            String accessToken = jwtTokenProvider.generateAccessToken(jwtPayload);

            assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        }
    }
}