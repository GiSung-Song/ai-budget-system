package com.budget.ai.logging;

import com.budget.ai.logging.aop.dto.LogDto;
import org.slf4j.MDC;

import java.time.ZonedDateTime;

public class LogMdcUtil {

    private static final String LOG_TYPE = "logType";
    private static final String EVENT_NAME = "eventName";
    private static final String TIMESTAMP = "timestamp";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    public static void setMdcField(LogDto dto) {
        MDC.put(LOG_TYPE, dto.logType());
        MDC.put(EVENT_NAME, dto.eventName());
        MDC.put(TIMESTAMP, dto.timestamp() != null ? dto.timestamp() : ZonedDateTime.now().toString());
        MDC.put(SUCCESS, dto.successLog() ? "SUCCESS" : "FAIL");
        MDC.put(MESSAGE, dto.message() != null ? dto.message() : "");
    }

    public static void clearMdcField() {
        MDC.remove(LOG_TYPE);
        MDC.remove(EVENT_NAME);
        MDC.remove(TIMESTAMP);
        MDC.remove(SUCCESS);
        MDC.remove(MESSAGE);
    }
}
