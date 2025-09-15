package com.budget.ai.logging.aop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(description = "운영 로그 DTO")
public record OperationLogDto(
        @Schema(description = "Event")
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

        @Schema(description = "성공 여부")
        boolean successLog,

        @Schema(description = "메시지")
        String message
) {
    public static OperationLogDto successLog(
            String eventName, String traceId, String userId, Long duration,
            String className, String methodName, Object[] args
    ) {
        return new OperationLogDto(
                eventName, traceId, userId, ZonedDateTime.now().toString(),
                duration, className, methodName, args, true, "Success");
    }

    public static OperationLogDto failureLog(
            String eventName, String traceId, String userId, String className,
            String methodName, Object[] args, String message
    ) {
        return new OperationLogDto(
                eventName, traceId, userId, ZonedDateTime.now().toString(), null,
                className, methodName, args, false, "Error : " + message);
    }
}
