package com.budget.ai.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * 카테고리별 합계 조회 응답 DTO
 */
@Schema(description = "카테고리별 합계 조회 응답 DTO")
public record SumCategoryTransactionResponse(
        @Schema(description = "카테고리별 정보")
        List<SumCategoryInfo> sumCategoryInfoList,

        @Schema(description = "총 지출 금액", example = "150200.00")
        BigDecimal totalSum
) {
    public record SumCategoryInfo(
            @Schema(description = "카테고리 식별 ID", example = "1")
            Long categoryId,

            @Schema(description = "카테고리 이름", example = "교통")
            String categoryName,

            @Schema(description = "총 거래 금액", example = "12500.50")
            BigDecimal sumAmount,

            @Schema(description = "총 거래 수", example = "32")
            Long transactionCount,

            @Schema(description = "카테고리별 금액 비율", example = "40.8")
            BigDecimal ratio
    ) { }
}
