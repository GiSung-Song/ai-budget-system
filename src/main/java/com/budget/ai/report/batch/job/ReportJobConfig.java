package com.budget.ai.report.batch.job;

import com.budget.ai.report.batch.processor.ReportProcessor;
import com.budget.ai.report.batch.reader.ReportReader;
import com.budget.ai.report.batch.writer.ReportWriter;
import com.budget.ai.report.dto.CategoryComparisonResult;
import com.budget.ai.report.dto.UserReportInput;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class ReportJobConfig {

    @Bean
    public Job saveReportJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("saveReportJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step saveReportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ReportReader reportReader,
            ReportProcessor reportProcessor,
            ReportWriter reportWriter
    ) {
        return new StepBuilder("saveReportStep", jobRepository)
                .<UserReportInput, CategoryComparisonResult>chunk(100, transactionManager)
                .reader(reportReader)
                .processor(reportProcessor)
                .writer(reportWriter)
                .build();
    }
}
