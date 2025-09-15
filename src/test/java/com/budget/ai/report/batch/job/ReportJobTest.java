package com.budget.ai.report.batch.job;

import com.budget.ai.card.Card;
import com.budget.ai.card.CardCompanyType;
import com.budget.ai.category.Category;
import com.budget.ai.category.CategoryRepository;
import com.budget.ai.report.ReportRepository;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.transaction.Transaction;
import com.budget.ai.transaction.TransactionRepository;
import com.budget.ai.transaction.TransactionStatus;
import com.budget.ai.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ReportJobTest {

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
    @Qualifier("saveReportJob")
    private Job saveReportJob;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        Category cafe = categoryRepository.findByCode("CAFE")
                .orElseThrow();

        Category transportation = categoryRepository.findByCode("TRANSPORTATION")
                .orElseThrow();

        Category food = categoryRepository.findByCode("FOOD")
                .orElseThrow();

        Category mart = categoryRepository.findByCode("MART")
                .orElseThrow();

        Category culture = categoryRepository.findByCode("CULTURE")
                .orElseThrow();


        User tester = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
        User dancer = testDataFactory.createUser("댄서", "dancer@email.com", "rawPassword");

        Card testerKB = testDataFactory.createCard(CardCompanyType.KB, "1234", tester);
        Card testerSAMSUNG = testDataFactory.createCard(CardCompanyType.SAMSUNG, "5678", tester);
        Card dancerHYUNDAI = testDataFactory.createCard(CardCompanyType.HYUNDAI, "1357", dancer);
        Card dancerNH = testDataFactory.createCard(CardCompanyType.NH, "2468", dancer);

        Transaction tr1 = Transaction.builder()
                .user(tester)
                .card(testerKB)
                .category(cafe)
                .merchantId("cafe-approved-001")
                .amount(new BigDecimal("13500.00"))
                .merchantName("이디야")
                .transactionAt(LocalDateTime.of(2025, 7, 8, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        Transaction tr2 = Transaction.builder()
                .user(tester)
                .card(testerKB)
                .category(cafe)
                .merchantId("cafe-canceled-001")
                .originalMerchantId("cafe-approved-001")
                .amount(new BigDecimal("13500.00"))
                .merchantName("이디야")
                .transactionAt(LocalDateTime.of(2025, 7, 8, 23, 30))
                .transactionStatus(TransactionStatus.CANCELED)
                .build();

        Transaction tr3 = Transaction.builder()
                .user(tester)
                .card(testerKB)
                .category(mart)
                .merchantId("cafe-approved-002")
                .amount(new BigDecimal("58000.00"))
                .merchantName("홈플러스")
                .transactionAt(LocalDateTime.of(2025, 8, 8, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        Transaction tr4 = Transaction.builder()
                .user(tester)
                .card(testerSAMSUNG)
                .category(culture)
                .merchantId("cafe-approved-003")
                .amount(new BigDecimal("70000.00"))
                .merchantName("CGV")
                .transactionAt(LocalDateTime.of(2025, 7, 8, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        Transaction tr5 = Transaction.builder()
                .user(dancer)
                .card(dancerNH)
                .category(cafe)
                .merchantId("cafe-approved-004")
                .amount(new BigDecimal("8000.00"))
                .merchantName("이디야")
                .transactionAt(LocalDateTime.of(2025, 7, 6, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        Transaction tr6 = Transaction.builder()
                .user(dancer)
                .card(dancerNH)
                .category(cafe)
                .merchantId("cafe-approved-005")
                .amount(new BigDecimal("80000.00"))
                .merchantName("이디야")
                .transactionAt(LocalDateTime.of(2025, 8, 6, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        Transaction tr7 = Transaction.builder()
                .user(dancer)
                .card(dancerNH)
                .category(transportation)
                .merchantId("cafe-approved-006")
                .amount(new BigDecimal("97000.00"))
                .merchantName("카카오 택시")
                .transactionAt(LocalDateTime.of(2025, 7, 6, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        Transaction tr8 = Transaction.builder()
                .user(dancer)
                .card(dancerHYUNDAI)
                .category(food)
                .merchantId("cafe-approved-007")
                .amount(new BigDecimal("130000.00"))
                .merchantName("투다리")
                .transactionAt(LocalDateTime.of(2025, 8, 6, 23, 30))
                .transactionStatus(TransactionStatus.APPROVED)
                .build();

        List<Transaction> transactionList = List.of(tr1, tr2, tr3, tr4, tr5, tr6, tr7, tr8);

        transactionRepository.saveAll(transactionList);
    }

    @AfterAll
    static void stop() {
        E2E_MYSQL.stop();
    }

    @Test
    void 리포트_저장_테스트() throws Exception {
        Map<String, JobParameter<?>> param = new HashMap<>();
        param.put("startDate", new JobParameter<>("2025-07-01T00:00:00", String.class));
        param.put("endDate", new JobParameter<>("2025-08-31T23:59:59", String.class));

        JobParameters jobParameters = new JobParameters(param);

        JobExecution jobExecution = jobLauncher.run(saveReportJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(reportRepository.findAll()).hasSize(2);
    }
}