package com.budget.ai.external.transaction.dto.response;

import com.budget.ai.external.transaction.CardTransaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 카드 거래 내역 조회 응답 DTO
 */
@Schema(description = "카드 거래 내역 조회 응답 DTO")
public record CardTransactionResponse(
        @Schema(description = "카드 거래 목록")
        List<CardTransactionInfo> cardTransactionList
) {
    public record CardTransactionInfo(
            @Schema(description = "카드사 거래 고유 ID", example = "TXN123456789")
            String merchantId,

            @Schema(description = "환불/취소 시 참조하는 카드사 거래 고유 ID", example = "TXN123456780")
            String originalMerchantId,

            @Schema(description = "카드 번호", example = "123412341234")
            String cardNumber,

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
        public static CardTransactionInfo from(CardTransaction cardTransaction) {
            return new CardTransactionInfo(
                    cardTransaction.getMerchantId(),
                    cardTransaction.getOriginalMerchantId(),
                    cardTransaction.getCardNumber(),
                    cardTransaction.getAmount(),
                    cardTransaction.getMerchantName(),
                    cardTransaction.getMerchantAddress(),
                    cardTransaction.getTransactionAt()
                            .atOffset(ZoneOffset.UTC),
                    cardTransaction.getCardTransactionType().name(),
                    cardTransaction.getCardTransactionStatus().name()
            );
        }
    }
}
