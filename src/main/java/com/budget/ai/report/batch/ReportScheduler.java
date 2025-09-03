package com.budget.ai.report.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ReportScheduler {

    private final JobLauncher jobLauncher;
    private final Job saveReportJob;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 매달 2일 00:00에 실행
     */
    @Scheduled(cron = "0 0 0 2 * ?", zone = "UTC")
    public void runMonthlyReportJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        YearMonth twoMonthsAgo = YearMonth.now(ZoneOffset.UTC).minusMonths(2);
        YearMonth oneMonthAgo = YearMonth.now(ZoneOffset.UTC).minusMonths(1);

        LocalDateTime startDate = twoMonthsAgo.atDay(1).atStartOfDay();
        LocalDateTime endDate = oneMonthAgo.atEndOfMonth().atTime(LocalTime.MAX);

        Map<String, JobParameter<?>> param = new HashMap<>();
        param.put("startDate", new JobParameter(startDate.format(FORMATTER), String.class));
        param.put("endDate", new JobParameter(endDate.format(FORMATTER), String.class));

        JobParameters jobParameters = new JobParameters(param);

        jobLauncher.run(saveReportJob, jobParameters);
    }
}
