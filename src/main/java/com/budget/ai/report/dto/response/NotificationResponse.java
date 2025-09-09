package com.budget.ai.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 알림 목록 조회 응답 DTO
 */
@Schema(description = "알림 목록 조회 응답 DTO")
public record NotificationResponse(
        @Schema(description = "알림 정보 목록")
        List<NotificationInfo> notificationInfoList
) {
    public record NotificationInfo(
            @Schema(description = "리포트 식별자 ID")
            Long reportId,

            @Schema(description = "알림 메시지")
            String notificationMessage
    ) {}
}