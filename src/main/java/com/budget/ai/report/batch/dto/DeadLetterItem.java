package com.budget.ai.report.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 배치 실패 시 수동 재실행 DTO
 */
@Schema(description = "배치 실패 시 수동 재실행 DTO")
public record DeadLetterItem(
        @Schema(description = "배치 실패 식별자 ID")
        Long id,

        @Schema(description = "배치용 사용자-카테고리별 합계 DTO")
        UserReportInput input
) {
}
