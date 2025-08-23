package com.budget.ai.transaction.dto.request;

import com.budget.ai.transaction.TransactionStatus;
import com.budget.ai.transaction.TransactionType;
import com.budget.ai.valid.ValidEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 카드 거래내역 조회 요청 파라미터 DTO
 */
@Schema(description = "카드 거래내역 조회 요청 파라미터 DTO")
public record TransactionQueryRequest(

        @Schema(description = "카드 고유 ID", example = "1")
        @Positive(message = "카드 고유 ID는 양수입니다.")
        Long cardId,

        @Schema(description = "조회 시작 날짜", example = "2025-03-20")
        @NotNull(message = "시작 날짜는 필수입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @Schema(description = "조회 종료 날짜", example = "2025-03-30")
        @NotNull(message = "종료 날짜는 필수입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Schema(description = "카테고리 고유 ID", example = "3")
        @Positive(message = "카테고리 고유 ID는 양수입니다.")
        Long categoryId,

        @Schema(description = "거래 상태", example = "APPROVED")
        TransactionStatus transactionStatus,

        @Schema(description = "거래 타입", example = "PAYMENT")
        TransactionType transactionType,

        @Schema(description = "상호명", example = "스타벅스 도봉점")
        String merchantName,

        @Schema(description = "최소 금액", example = "1000.00")
        @PositiveOrZero(message = "최소 금액은 0입니다.")
        BigDecimal amountMin,

        @Schema(description = "최대 금액", example = "1000.00")
        @PositiveOrZero(message = "최대 금액은 0입니다.")
        BigDecimal amountMax,

        @Schema(description = "정렬 순서", example = "ASC")
        SortOrder sortOrder,

        @PositiveOrZero(message = "page는 양수입니다.")
        int page,

        @Positive(message = "size는 양수입니다.")
        int size
) {
    public enum SortOrder {
        ASC, DESC
    }
}
