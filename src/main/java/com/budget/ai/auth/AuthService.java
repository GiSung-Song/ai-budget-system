package com.budget.ai.auth;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budget.ai.auth.dto.request.JwtPayload;
import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 인증 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 로그인
     *
     * @param request 로그인 요청 데이터
     * @return 토큰 응답 데이터
     * @throws CustomException 로그인 실패 시 예외 발생
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_REQUEST);
        }

        JwtPayload jwtPayload = new JwtPayload(user.getId(), user.getName(), user.getEmail());

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(jwtPayload);
        String refreshToken = jwtTokenProvider.generateRefreshToken(jwtPayload);

        String redisKey = "refresh:" + user.getId();

        redisTemplate.opsForValue().set(redisKey, refreshToken, jwtTokenProvider.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 토큰 재발급
     *
     * @param refreshToken 리프레시 토큰
     * @return 토큰 응답 데이터
     * @throws CustomException 토큰 재발급 실패 시 예외 발생
     */
    public TokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.parseRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (storedRefreshToken == null || !refreshToken.equals(storedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        JwtPayload jwtPayload = new JwtPayload(user.getId(), user.getName(), user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(jwtPayload);

        return new TokenResponse(accessToken, null);
    }

    /**
     * 로그아웃 (토큰 블랙리스트 처리)
     *
     * @param accessToken 액세스 토큰
     * @throws CustomException 로그아웃 실패 시 예외 발생
     */
    public void logout(String accessToken) {
        Date expiration = jwtTokenProvider.getTokenExpiration(accessToken);

        long expirationTime = expiration.getTime() - System.currentTimeMillis();
        jwtTokenProvider.tokenToHash(accessToken).ifPresent(hash -> {
            redisTemplate.opsForValue().set(hash, "logout", expirationTime, TimeUnit.MILLISECONDS);
        });
    }
} 