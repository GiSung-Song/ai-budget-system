package com.budget.ai.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 응답 DTO
 */
@Schema(description = "토큰 응답 DTO")
public record TokenResponse(

    @Schema(description = "액세스 토큰", example = "ey...")
    String accessToken,

    @Schema(description = "리프레시 토큰", example = "ey...")
    String refreshToken
) {
} 