package com.budget.ai.user.dto.response;

import com.budget.ai.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 회원 정보 조회 응답 DTO
 * <p>
 * - 이메일, 비밀번호, 생성일자 필드 포함
 * </p>
 */
@Schema(description = "회원 정보 조회 응답 DTO")
public record UserInfoResponse(

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "test@example.com")
        String email,

        @Schema(description = "생성일자", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
