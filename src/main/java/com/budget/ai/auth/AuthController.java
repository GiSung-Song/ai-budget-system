package com.budget.ai.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터(필수값 누락 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패(이메일/비밀번호 불일치 등)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<SuccessResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);

        // Refresh Token을 쿠키에 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenResponse.refreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
        refreshTokenCookie.setPath("/api/auth/refresh");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(SuccessResponse.of(tokenResponse));
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터(쿠키 누락 등)"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰(만료, 위조, 블랙리스트 등)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<SuccessResponse<TokenResponse>> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(SuccessResponse.of(tokenResponse));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃하고 토큰을 무효화합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰(만료, 위조, 블랙리스트 등)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<SuccessResponse<Void>> logout(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);

        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(SuccessResponse.of());
    }
} 