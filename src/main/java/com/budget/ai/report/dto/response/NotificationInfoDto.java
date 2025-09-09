package com.budget.ai.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 알림 목록 정보 조회 DTO
 */
@Schema(description = "알림 목록 정보 조회 DTO")
public record NotificationInfoDto(
        @Schema(description = "리포트 식별자 ID")
        Long reportId,

        @Schema(description = "알림 메시지")
        String notificationMessage
) {
}
