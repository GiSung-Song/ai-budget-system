package com.budget.ai.logging.aop.dto;

public interface LogDto {
    String logType();
    String eventName();
    String timestamp();
    boolean successLog();
    String message();
}
