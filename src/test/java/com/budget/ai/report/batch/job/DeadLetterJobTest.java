package com.budget.ai.report.batch.job;

import com.budget.ai.report.ReportRepository;
import com.budget.ai.report.batch.dto.UserReportInput;
import com.budget.ai.transaction.dto.TransactionReportDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class DeadLetterJobTest {

    static MySQLContainer<?> E2E_MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("e2edb")
            .withUsername("root")
            .withPassword("password");

    @DynamicPropertySource
    static void registerMySQL(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", E2E_MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", E2E_MYSQL::getUsername);
        registry.add("spring.datasource.password", E2E_MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", E2E_MYSQL::getDriverClassName);
    }

    static {
        E2E_MYSQL.start();
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("deadLetterRetryJob")
    private Job deadLetterRetryJob;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        UserReportInput userReportInput = new UserReportInput(
                1L,
                List.of(
                        new TransactionReportDto("카페", new BigDecimal("13500.00"), "2025-07"),
                        new TransactionReportDto("마트", new BigDecimal("50000.00"), "2025-07"),
                        new TransactionReportDto("교통", new BigDecimal("38000.00"), "2025-08"),
                        new TransactionReportDto("마트", new BigDecimal("18000.00"), "2025-08")
                )
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(userReportInput);

        jdbcTemplate.update(
                "INSERT INTO BATCH_DEAD_LETTER (input_data) VALUES (?)",
                json
        );
    }

    @Test
    void 실패_배치_수동_재실행_테스트() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(deadLetterRetryJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(reportRepository.findAll()).hasSize(1);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_DEAD_LETTER",
                Integer.class
        );

        assertThat(count).isZero();
    }
}
