package com.budget.ai.report.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 배치용 사용자-카테고리별 전달과 전전달의 차이점 DTO
 */
@Schema(description = "배치용 사용자-카테고리별 전달과 전전달의 차이점 DTO")
public record CategoryComparisonResult(

        @Schema(description = "사용자 식별자 ID")
        Long userId,

        @Schema(description = "해당 년월")
        LocalDate yearMonth,

        @Schema(description = "리포트 내용")
        String reportMessage,

        @Schema(description = "알림 메시지")
        String notificationMessage
) {
}
