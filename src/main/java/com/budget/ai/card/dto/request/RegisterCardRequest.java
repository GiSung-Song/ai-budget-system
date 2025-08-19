package com.budget.ai.card.dto.request;

import com.budget.ai.card.CardCompanyType;
import com.budget.ai.valid.ValidEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 카드 등록 요청 DTO
 * <p>
 * - 카드사, 카드번호 필드 포함
 * </p>
 */
@Schema(description = "카드 등록 요청 DTO")
public record RegisterCardRequest(

        @Schema(description = "카드사", example = "SAMSUNG")
        @NotBlank(message = "카드사는 필수 입력 필드입니다.")
        @ValidEnum(enumClass = CardCompanyType.class, message = "유효하지 않은 카드사입니다.")
        String cardCompanyType,

        @Schema(description = "카드 번호", example = "123412341234")
        @NotBlank(message = "카드 번호는 필수 입력 필드입니다.")
        @Size(min = 10, max = 20, message = "카드 번호는 10자 이상 20자 이하여야 합니다.")
        String cardNumber
) {
}