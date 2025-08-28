package com.budget.ai.external.transaction.dto.request;

import com.budget.ai.card.CardCompanyType;
import com.budget.ai.external.transaction.CardTransactionStatus;
import com.budget.ai.valid.ValidEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 카드 거래내역 생성 요청 DTO
 * <p>
 * - 카드사, 카드번호 필드 포함
 * </p>
 */
@Schema(description = "카드 등록 요청 DTO")
public record AddCardTransactionRequest(

        @Schema(description = "카드사 거래 고유 ID", example = "TXN123456789")
        @NotBlank(message = "카드사 거래 고유 ID는 필수 입력 필드입니다.")
        String merchantId,

        @Schema(description = "환불/취소 시 참조하는 카드사 거래 고유 ID", example = "TXN123456780")
        String originalMerchantId,

        @Schema(description = "카드사", example = "SAMSUNG")
        @NotBlank(message = "카드사는 필수 입력 필드입니다.")
        @ValidEnum(enumClass = CardCompanyType.class)
        String cardCompanyType,

        @Schema(description = "카드 번호", example = "123412341234")
        @NotBlank(message = "카드 번호는 필수 입력 필드입니다.")
        @Size(min = 10, max = 20, message = "카드 번호는 10자 이상 20자 이하여야 합니다.")
        String cardNumber,

        @Schema(description = "거래 금액", example = "12500.50")
        @NotNull(message = "거래 금액은 필수 입력 필드입니다.")
        @DecimalMin(value = "0.01", message = "거래 금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @Schema(description = "가게명", example = "스타벅스 강남역점")
        @NotBlank(message = "가게명은 필수 입력 필드입니다.")
        String merchantName,

        @Schema(description = "가게 주소", example = "서울특별시 강남구 강남대로 123")
        String merchantAddress,

        @Schema(description = "거래 시각", example = "2025-08-18T12:34:56+09:00")
        @NotNull(message = "거래 시각은 필수 입력 필드입니다.")
        OffsetDateTime transactionAt,

        @Schema(description = "거래 상태", example = "APPROVED")
        @NotNull(message = "거래 상태는 필수 입력 필드입니다.")
        @ValidEnum(enumClass = CardTransactionStatus.class)
        String cardTransactionStatus
) {
}
