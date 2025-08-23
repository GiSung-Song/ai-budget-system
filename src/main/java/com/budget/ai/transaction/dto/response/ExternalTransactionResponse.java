package com.budget.ai.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 카드 거래 내역 조회 응답 DTO
 */
@Schema(description = "카드 거래 내역 조회 응답 DTO")
public record ExternalTransactionResponse(
        @Schema(description = "카드 거래 목록")
        List<TransactionInfo> cardTransactionList
) {
    public record TransactionInfo(
            @Schema(description = "카드사 거래 고유 ID", example = "TXN123456789")
            String merchantId,

            @Schema(description = "환불/취소 시 참조하는 카드사 거래 고유 ID", example = "TXN123456780")
            String originalMerchantId,

            @Schema(description = "거래 금액", example = "12500.50")
            BigDecimal amount,

            @Schema(description = "가게명", example = "스타벅스 강남역점")
            String merchantName,

            @Schema(description = "가게 주소", example = "서울특별시 강남구 강남대로 123")
            String merchantAddress,

            @Schema(description = "거래 시각", example = "2025-08-18T12:34:56Z")
            OffsetDateTime transactionAt,

            @Schema(description = "거래 유형", example = "PAYMENT")
            String cardTransactionType,

            @Schema(description = "거래 상태", example = "APPROVED")
            String cardTransactionStatus
    ) {
    }
}
