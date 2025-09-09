package com.budget.ai.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 리포트 조회 응답 DTO
 */
@Schema(description = "리포트 조회 응답 DTO")
public record ReportResponse(
        @Schema(description = "리포트 내용")
        String reportMessage
) {
}
