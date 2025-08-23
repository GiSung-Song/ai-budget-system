package com.budget.ai.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 수정 요청 DTO
 * <p>
 * - 현재 비밀번호, 새로운 비밀번호 필드 포함
 * </p>
 */
@Schema(description = "비밀번호 수정 요청 DTO")
public record PasswordUpdateRequest(

        @Schema(description = "현재 비밀번호", example = "password123")
        @NotBlank(message = "현재 비밀번호는 필수 입력 필드입니다.")
        @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
        String currentPassword,

        @Schema(description = "새로운 비밀번호", example = "password123")
        @NotBlank(message = "새로운 비밀번호는 필수 입력 필드입니다.")
        @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
        String newPassword
) {
}
