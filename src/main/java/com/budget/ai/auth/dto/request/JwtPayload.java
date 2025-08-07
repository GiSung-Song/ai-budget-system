package com.budget.ai.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 페이로드 DTO")
public record JwtPayload(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "사용자 이메일", example = "test@test.com")
        String email
) {
}
