package com.budget.ai.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 * <p>
 * - 이메일, 비밀번호, 이름 필드 포함
 * - 이메일 형식 검증
 * - 비밀번호 길이 검증
 * - 이름 길이 검증
 * </p>
 */
@Schema(description = "회원가입 요청 DTO")
public record RegisterRequest(
    @Schema(description = "이메일", example = "test@example.com")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 필드입니다.")
    String email,

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수 입력 필드입니다.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
    String password,

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수 입력 필드입니다.")
    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하여야 합니다.")
    String name
) {}
