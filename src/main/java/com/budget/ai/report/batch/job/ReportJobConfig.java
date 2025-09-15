package com.budget.ai.report.batch.job;

import com.budget.ai.report.batch.listener.DeadLetterSkipListener;
import com.budget.ai.report.batch.dto.CategoryComparisonResult;
import com.budget.ai.report.batch.dto.UserReportInput;
import com.budget.ai.report.batch.listener.JobLoggerListener;
import com.budget.ai.report.batch.listener.StepLoggerListener;
import com.budget.ai.report.batch.processor.ReportProcessor;
import com.budget.ai.report.batch.reader.ReportReader;
import com.budget.ai.report.batch.writer.ReportWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class ReportJobConfig {

    @Bean
    @Qualifier("saveReportJob")
    public Job saveReportJob(JobRepository jobRepository, Step saveReportStep, JobLoggerListener jobLoggerListener) {
        return new JobBuilder("saveReportJob", jobRepository)
                .listener(jobLoggerListener)
                .start(saveReportStep)
                .build();
    }

    @Bean
    @Qualifier("saveReportStep")
    public Step saveReportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ReportReader reportReader,
            ReportProcessor reportProcessor,
            ReportWriter reportWriter,
            DeadLetterSkipListener deadLetterSkipListener,
            StepLoggerListener stepLoggerListener
    ) {
        return new StepBuilder("saveReportStep", jobRepository)
                .<UserReportInput, CategoryComparisonResult>chunk(100, transactionManager)
                .reader(reportReader)
                .processor(reportProcessor)
                .writer(reportWriter)

                // Retry
                .faultTolerant()
                .retryLimit(3) // 최대 3회 재시도
                .retry(TransientDataAccessException.class)
                .retry(CannotAcquireLockException.class)

                // Skip
                .skipLimit(100) // 최대 100건까지 Skip
                .skip(DataIntegrityViolationException.class)
                .listener(deadLetterSkipListener)

                // logging
                .listener(stepLoggerListener)

                .build();
    }
}