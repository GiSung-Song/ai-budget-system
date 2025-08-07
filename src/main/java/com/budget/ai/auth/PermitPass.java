package com.budget.ai.auth;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

/**
 * 인증 예외 경로
 * 
 * @param method 요청 메서드
 * @param pathPrefix 요청 경로 접두사
 */
public record PermitPass(HttpMethod method, String pathPrefix) {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 요청 경로와 허용된 경로 비교
     * 
     * @param requestMethod 요청 메서드
     * @param requestPath 요청 경로
     * @return 요청 경로와 허용된 경로가 일치하면 true, 아니면 false
     */
    public boolean matches(String requestMethod, String requestPath) {
        return (method == null || method.name().equalsIgnoreCase(requestMethod))
                && matcher.match(pathPrefix, requestPath);
    }
} 