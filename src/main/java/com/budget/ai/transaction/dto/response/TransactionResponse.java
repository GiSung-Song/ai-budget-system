package com.budget.ai.transaction.dto.response;

import com.budget.ai.transaction.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 거래 내역 조회 응답 DTO
 */
@Schema(description = "거래 내역 조회 응답 DTO")
public record TransactionResponse(
        @Schema(description = "거래 목록")
        List<TransactionInfo> transactionList,

        @Schema(description = "총 개수")
        long totalElements,

        @Schema(description = "총 페이지 수")
        int totalPages,

        @Schema(description = "페이지")
        int page,

        @Schema(description = "페이지 당 개수")
        int size
) {
    public record TransactionInfo(
            @Schema(description = "카드 번호", example = "123412341234")
            String cardNumber,

            @Schema(description = "카테고리", example = "교통")
            String categoryName,

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

            @Schema(description = "거래 상태", example = "APPROVED")
            String cardTransactionStatus
    ) {
        public static TransactionInfo from(Transaction transaction) {
            return new TransactionInfo(
                    transaction.getCard().getCardNumber(),
                    transaction.getCategory().getDisplayName(),
                    transaction.getMerchantId(),
                    transaction.getOriginalMerchantId(),
                    transaction.getAmount(),
                    transaction.getMerchantName(),
                    transaction.getMerchantAddress(),
                    transaction.getTransactionAt().atOffset(ZoneOffset.UTC),
                    transaction.getTransactionStatus().getDisplayName()
            );
        }
    }
}
