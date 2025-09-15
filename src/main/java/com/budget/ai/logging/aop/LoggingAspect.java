package com.budget.ai.logging.aop;

import com.budget.ai.logging.MaskingUtil;
import com.budget.ai.logging.aop.dto.AuditLogDto;
import com.budget.ai.logging.aop.dto.OperationLogDto;
import com.budget.ai.response.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private static final String MDC_UUID_KEY = "requestId";
    private static final String MDC_IP_KEY = "clientIp";
    private static final String USER_ID_KEY = "userId";

    @Around("@annotation(auditLog)")
    public Object saveAuditLog(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String eventName = auditLog.eventName();
        String operationType = auditLog.operationType();
        String entity = auditLog.entity();

        String traceId = getTraceId();
        String userId = getUserId();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        String clientIp = MDC.get(MDC_IP_KEY);

        String entityId = "";

        Object[] args = joinPoint.getArgs();
        Object[] maskingArgs = MaskingUtil.makeMaskingData(args);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(AuditLEntityId.class)) {
                entityId = args[i] instanceof Long ? String.valueOf(args[i]) : "";
                break;
            }
        }

        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            String message = getExceptionMessage(e);

            AuditLogDto failureLog = AuditLogDto.failureLog(
                    eventName, traceId, userId, className,
                    methodName, maskingArgs, operationType,
                    entity, entityId, message, clientIp
            );
            String jsonData = objectMapper.writeValueAsString(failureLog);
            log.error(jsonData, e);

            throw e;
        }

        long duration = System.currentTimeMillis() - start;

        AuditLogDto successLog = AuditLogDto.successLog(
                eventName, traceId, userId, duration,
                className, methodName, maskingArgs,
                operationType, entity, entityId, clientIp
        );
        String successJsonLog = objectMapper.writeValueAsString(successLog);
        log.info(successJsonLog);

        return result;
    }

    @Around("@annotation(operationLog)")
    public Object saveOperationLog(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String eventName = operationLog.eventName();

        String traceId = getTraceId();
        String userId = getUserId();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();
        Object[] maskingArgs = MaskingUtil.makeMaskingData(args);

        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            String message = getExceptionMessage(e);

            OperationLogDto failureLog = OperationLogDto.failureLog(
                    eventName, traceId, userId, className,
                    methodName, maskingArgs, message
            );
            String jsonData = objectMapper.writeValueAsString(failureLog);
            log.error(jsonData, e);

            throw e;
        }

        long duration = System.currentTimeMillis() - start;

        OperationLogDto successLog = OperationLogDto.successLog(
                eventName, traceId, userId, duration,
                className, methodName, maskingArgs
        );
        String successJsonLog = objectMapper.writeValueAsString(successLog);
        log.info(successJsonLog);

        return result;
    }

    private String getTraceId() {
        return MDC.get(MDC_UUID_KEY);
    }

    private String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    private String getExceptionMessage(Exception e) {
        if (e instanceof CustomException ce) {
            return "ErrorCode: " + ce.getErrorCode().getCode() + ", Message: " + ce.getErrorCode().getMessage();
        }
        return e.getMessage();
    }
}
