package com.budget.ai.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * 카테고리별 합계 조회 DTO
 */
@Schema(description = "카테고리별 합계 조회 DTO")
public record SumCategoryTransaction(
        @Schema(description = "카테고리별 정보")
        List<CategoryInfo> categoryInfoList
) {
        public record CategoryInfo(
                @Schema(description = "카테고리 식별 ID", example = "1")
                Long categoryId,

                @Schema(description = "카테고리 이름", example = "교통")
                String categoryName,

                @Schema(description = "총 거래 금액", example = "12500.50")
                BigDecimal sumAmount,

                @Schema(description = "총 거래 수", example = "32")
                Long transactionCount
        ) { }
}
