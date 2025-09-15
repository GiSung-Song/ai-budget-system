package com.budget.ai.auth;

import com.budget.ai.auth.dto.request.JwtPayload;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private final List<PermitPass> PASS_PATHS = List.of(
            new PermitPass(HttpMethod.POST, "/api/auth/login"),
            new PermitPass(HttpMethod.POST, "/api/auth/refresh"),
            new PermitPass(HttpMethod.POST, "/api/users"),
            new PermitPass(HttpMethod.DELETE, "/api/users/me/deletion"),
            new PermitPass(null, "/v3/api-docs/**"),
            new PermitPass(null, "/swagger-ui/**"),
            new PermitPass(null, "/swagger-ui.html"),
            new PermitPass(null, "/swagger-resources/**"),
            new PermitPass(null, "/webjars/**"),
            new PermitPass(null, "/favicon.ico"),
            new PermitPass(null, "/outer/transaction")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String requestHeader = request.getHeader("Authorization");

        boolean isPermitPass = PASS_PATHS.stream().anyMatch(p -> p.matches(method, requestURI));
        boolean hasToken = StringUtils.hasText(requestHeader) && requestHeader.startsWith("Bearer ");

        if (hasToken) {
            String accessToken = requestHeader.substring(7);

            if (!jwtTokenProvider.validateToken(accessToken) || isLogout(accessToken)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            JwtPayload jwtPayload = jwtTokenProvider.parseAccessToken(accessToken);
            Long userId = jwtPayload.id();
            String name = jwtPayload.name();
            String email = jwtPayload.email();

            CustomUserDetails customUserDetails = new CustomUserDetails(userId, name, email);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            SecurityContextHolder.clearContext();
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } else {
            if (!isPermitPass) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLogout(String accessToken) {
        String hashToken = jwtTokenProvider.tokenToHash(accessToken).orElse(null);
        return hashToken != null && redisTemplate.opsForValue().get(hashToken) != null;
    }
} 