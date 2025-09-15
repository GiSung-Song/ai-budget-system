package com.budget.ai.report;

import com.budget.ai.logging.aop.OperationLog;
import com.budget.ai.report.dto.response.NotificationInfoDto;
import com.budget.ai.report.dto.response.NotificationResponse;
import com.budget.ai.report.dto.response.ReportResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 리포트 조회 Service
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final JobLauncher jobLauncher;
    private final Job reportBatchJob;
    private final Job deadLetterRetryJob;

    public ReportService(ReportRepository reportRepository,
                         JobLauncher jobLauncher,
                         @Qualifier("saveReportJob") Job reportBatchJob,
                         @Qualifier("deadLetterRetryJob") Job deadLetterRetryJob) {
        this.reportRepository = reportRepository;
        this.jobLauncher = jobLauncher;
        this.reportBatchJob = reportBatchJob;
        this.deadLetterRetryJob = deadLetterRetryJob;
    }

    /**
     * 리포트 알림 목록 조회
     * @param userId 로그인한 사용자 ID
     * @return 알림 목록
     */
    @Transactional(readOnly = true)
    @OperationLog(eventName = "리포트 알림 목록 조회")
    public NotificationResponse getNotificationList(Long userId) {
        List<NotificationInfoDto> notificationDtoList = Optional.ofNullable(reportRepository.findAllNotificationByUserId(userId))
                .orElse(Collections.emptyList());

        List<NotificationResponse.NotificationInfo> notificationInfoList = notificationDtoList.stream()
                .map(dto -> new NotificationResponse.NotificationInfo(dto.reportId(), dto.notificationMessage()))
                .toList();

        return new NotificationResponse(notificationInfoList);
    }

    /**
     * 리포트 상세 조회
     * @param userId   로그인한 사용자 ID
     * @param reportId 리포트 ID
     * @return 리포트 상세
     */
    @Transactional(readOnly = true)
    @OperationLog(eventName = "리포트 상세 조회")
    public ReportResponse getReport(Long userId, Long reportId) {
        String reportMessage = reportRepository.findReportMessageByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        return new ReportResponse(reportMessage);
    }

    /**
     * 배치 수동 실행
     */
    @OperationLog(eventName = "리포트 생성 배치 수동 실행")
    public void runBatchJob() {
        YearMonth twoMonthsAgo = YearMonth.now(ZoneOffset.UTC).minusMonths(2);
        YearMonth oneMonthAgo = YearMonth.now(ZoneOffset.UTC).minusMonths(1);

        LocalDateTime startDate = twoMonthsAgo.atDay(1).atStartOfDay();
        LocalDateTime endDate = oneMonthAgo.atEndOfMonth().atTime(LocalTime.MAX);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        Map<String, JobParameter<?>> param = new HashMap<>();
        param.put("startDate", new JobParameter<>(startDateStr, String.class));
        param.put("endDate", new JobParameter<>(endDateStr, String.class));

        JobParameters jobParameters = new JobParameters(param);

        try {
            JobExecution jobExecution = jobLauncher.run(reportBatchJob, jobParameters);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.BATCH_RUN_ERROR);
        }
    }

    /**
     * 배치 실패한 작업들 수동 재실행
     */
    @OperationLog(eventName = "리포트 생성 배치 실패 건 수동 실행")
    public void runDeadLetterBatchJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(deadLetterRetryJob, jobParameters);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.BATCH_RUN_ERROR);
        }
    }
}