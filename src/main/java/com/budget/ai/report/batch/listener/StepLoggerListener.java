package com.budget.ai.report.batch.listener;

import com.budget.ai.logging.aop.dto.OperationLogDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepLoggerListener implements StepExecutionListener {

    private final ObjectMapper objectMapper;
    private static final String MDC_UUID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String traceId = Optional.ofNullable(MDC.get(MDC_UUID_KEY))
                .orElse("batch-" + DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").format(ZonedDateTime.now()));

        String userId = Optional.ofNullable(MDC.get(USER_ID_KEY))
                .orElse("system");

        OperationLogDto dto = new OperationLogDto(
                "STEP-START",
                traceId,
                userId,
                ZonedDateTime.now().toString(),
                null,
                this.getClass().getSimpleName(),
                stepExecution.getStepName(),
                new Object[]{},
                true,
                "Batch Step Started"
        );

        log.info(serialize(dto));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String traceId = Optional.ofNullable(MDC.get(MDC_UUID_KEY))
                .orElse("batch-" + DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").format(ZonedDateTime.now()));

        String userId = Optional.ofNullable(MDC.get(USER_ID_KEY))
                .orElse("system");

        boolean success = stepExecution.getStatus() == BatchStatus.COMPLETED;

        long duration = 0;
        if (stepExecution.getStartTime() != null && stepExecution.getEndTime() != null) {
            duration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis();
        }

        OperationLogDto operationLogDto = success
                ? OperationLogDto.successLog(
                "STEP-END", traceId, userId, duration,
                this.getClass().getSimpleName(), stepExecution.getStepName(),
                new Object[]{stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getSkipCount()}
        )
                : OperationLogDto.failureLog(
                "STEP-END", traceId, userId,
                this.getClass().getSimpleName(), stepExecution.getStepName(),
                new Object[]{stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getSkipCount()},
                stepExecution.getStepName() + " 배치 작업이 실패했습니다."
        );

        if (success) log.info(serialize(operationLogDto));
        else log.error(serialize(operationLogDto));

        return stepExecution.getExitStatus();
    }

    private String serialize(OperationLogDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("Failed DTO to JSON Serialize", e);
            return "{}";
        }
    }
}
