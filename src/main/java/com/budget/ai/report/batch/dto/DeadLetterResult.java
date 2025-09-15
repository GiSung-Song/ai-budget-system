package com.budget.ai.report.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dead Letter Retry 배치 Result DTO
 */
@Schema(description = "Dead Letter Retry 배치 Result DTO")
public record DeadLetterResult(
        @Schema(description = "배치용 사용자-카테고리별 전달과 전전달의 차이점 DTO")
        CategoryComparisonResult result,

        @Schema(description = "배치 실패 식별자 ID")
        Long deadLetterId
) {
}
