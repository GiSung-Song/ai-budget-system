package com.budget.ai.logging.aop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(description = "감사 로그 DTO")
public record AuditLogDto(

        @Schema(description = "로그 타입")
        String logType,

        @Schema(description = "Event명")
        String eventName,

        @Schema(description = "추적용 UUID")
        String traceId,

        @Schema(description = "회원 식별자 ID")
        String userId,

        @Schema(description = "실행 시점")
        String timestamp,

        @Schema(description = "수행 시간(ms), 실패 시 null")
        Long duration,

        @Schema(description = "클래스 이름")
        String className,

        @Schema(description = "메서드 이름")
        String methodName,

        @Schema(description = "인자")
        Object[] args,

        @Schema(description = "CREATE / READ / UPDATE / DELETE 등")
        String operationType,

        @Schema(description = "대상 객체/테이블")
        String entity,

        @Schema(description = "대상 객체 ID")
        String entityId,

        @Schema(description = "성공 여부")
        boolean successLog,

        @Schema(description = "메시지")
        String message,

        @Schema(description = "요청 클라이언트 IP")
        String clientIP
) implements LogDto {
    public static AuditLogDto successLog(
            String eventName, String traceId, String userId, Long duration,
            String className, String methodName, Object[] args, String operationType,
            String entity, String entityId, String clientIP
    ) {
        return new AuditLogDto("AUDIT",
                eventName, traceId, userId, ZonedDateTime.now().toString(),
                duration, className, methodName, args, operationType,
                entity, entityId, true, "Success", clientIP);
    }

    public static AuditLogDto failureLog(
            String eventName, String traceId, String userId, String className,
            String methodName, Object[] args, String operationType,
            String entity, String entityId, String message, String clientIP
    ) {
        return new AuditLogDto("AUDIT",
                eventName, traceId, userId, ZonedDateTime.now().toString(), null,
                className, methodName, args, operationType, entity, entityId, false,
                "Error : " + message, clientIP);
    }
}
