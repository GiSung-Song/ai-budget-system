package com.budget.ai.logging;

import com.budget.ai.logging.aop.dto.AuditLogDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@RequiredArgsConstructor
public class AuditLogUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String MDC_UUID_KEY = "requestId";
    private static final String MDC_IP_KEY = "clientIp";
    private static final String USER_ID_KEY = "userId";

    public static void logAudit(Object target,
                                       Object[] args,
                                       String eventName,
                                       String methodName,
                                       String operationType,
                                       String entity,
                                       String entityId,
                                       String message,
                                       boolean success) {
        String traceId = MDC.get(MDC_UUID_KEY);
        String userId = MDC.get(USER_ID_KEY);
        String clientIP = MDC.get(MDC_IP_KEY);

        AuditLogDto auditLogDto = success
                ? AuditLogDto.successLog(eventName, traceId, userId, null, target.getClass().getSimpleName(),
                methodName, args, operationType, entity, entityId, clientIP)
                : AuditLogDto.failureLog(eventName, traceId, userId, target.getClass().getSimpleName(),
                methodName, args, operationType, entity, entityId, message, clientIP);

        try {
            String json = OBJECT_MAPPER.writeValueAsString(auditLogDto);

            if (success) log.info(json);
            else log.error(json);
        } catch (JsonProcessingException exception) {
            log.error("Failed DTO to JSON Serialize", exception);
        }
    }
}
