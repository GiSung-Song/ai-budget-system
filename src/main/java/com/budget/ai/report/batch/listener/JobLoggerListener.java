package com.budget.ai.report.batch.listener;

import com.budget.ai.logging.aop.dto.OperationLogDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobLoggerListener implements JobExecutionListener {

    private final ObjectMapper objectMapper;
    private static final String MDC_UUID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String traceId = Optional.ofNullable(MDC.get(MDC_UUID_KEY))
                .orElse("batch-" + DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").format(ZonedDateTime.now()));

        String userId = Optional.ofNullable(MDC.get(USER_ID_KEY))
                .orElse("system");

        OperationLogDto dto = new OperationLogDto(
                "JOB-START",
                traceId,
                userId,
                ZonedDateTime.now().toString(),
                null,
                this.getClass().getSimpleName(),
                jobExecution.getJobInstance().getJobName(),
                new Object[]{jobExecution.getJobParameters()},
                true,
                "Batch Job Started"
        );

        log.info(serialize(dto));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String traceId = Optional.ofNullable(MDC.get(MDC_UUID_KEY))
                .orElse("batch-" + DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").format(ZonedDateTime.now()));

        String userId = Optional.ofNullable(MDC.get(USER_ID_KEY))
                .orElse("system");

        boolean success = jobExecution.getStatus() == BatchStatus.COMPLETED;
        long duration = 0;
        if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
            duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
        }

        OperationLogDto operationLogDto = success
                ? OperationLogDto.successLog(
                "JOB-END",
                traceId,
                userId,
                duration,
                this.getClass().getSimpleName(),
                jobExecution.getJobInstance().getJobName(),
                new Object[]{jobExecution.getJobParameters()}
        )
                : OperationLogDto.failureLog(
                "JOB-END",
                traceId,
                userId,
                this.getClass().getSimpleName(),
                jobExecution.getJobInstance().getJobName(),
                new Object[]{jobExecution.getJobParameters()},
                jobExecution.getJobInstance().getJobName() + " 배치 작업이 실패했습니다."
        );

        if (success) log.info(serialize(operationLogDto));
        else log.error(serialize(operationLogDto));
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
