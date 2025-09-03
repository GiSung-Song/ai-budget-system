package com.budget.ai.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 리포트용 카테고리별 합계 조회 DTO
 */
@Schema(description = "리포트용 카테고리별 합계 조회 DTO")
public record TransactionReportDTO(
        @Schema(description = "카테고리명")
        String categoryName,

        @Schema(description = "카테고리별 총 금액")
        BigDecimal totalAmount,

        @Schema(description = "해당 년월")
        String month
) {
}
