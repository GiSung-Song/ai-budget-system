package com.budget.ai.report.batch.job;

import com.budget.ai.report.batch.listener.DeadLetterStepListener;
import com.budget.ai.report.batch.dto.DeadLetterItem;
import com.budget.ai.report.batch.dto.DeadLetterResult;
import com.budget.ai.report.batch.listener.JobLoggerListener;
import com.budget.ai.report.batch.listener.StepLoggerListener;
import com.budget.ai.report.batch.processor.DeadLetterProcessor;
import com.budget.ai.report.batch.reader.DeadLetterReader;
import com.budget.ai.report.batch.writer.DeadLetterWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class DeadLetterRetryJobConfig {

    @Bean
    @Qualifier("deadLetterRetryJob")
    public Job deadLetterRetryJob(JobRepository jobRepository, Step deadLetterStep, JobLoggerListener jobLoggerListener) {
        return new JobBuilder("deadLetterRetryJob", jobRepository)
                .listener(jobLoggerListener)
                .start(deadLetterStep)
                .build();
    }

    @Bean
    @Qualifier("deadLetterStep")
    public Step deadLetterStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DeadLetterReader deadLetterReader,
            DeadLetterProcessor deadLetterProcessor,
            DeadLetterWriter deadLetterWriter,
            DeadLetterStepListener deadLetterStepListener,
            StepLoggerListener stepLoggerListener
    ) {
        return new StepBuilder("deadLetterStep", jobRepository)
                .<DeadLetterItem, DeadLetterResult>chunk(50, transactionManager)
                .reader(deadLetterReader)
                .processor(deadLetterProcessor)
                .writer(deadLetterWriter)
                .listener(deadLetterStepListener)
                .listener(stepLoggerListener)
                .build();
    }
}
