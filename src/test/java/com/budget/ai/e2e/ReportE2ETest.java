package com.budget.ai.e2e;


import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.transaction.TransactionService;
import com.budget.ai.transaction.dto.request.TransactionSyncRequest;
import com.budget.ai.user.dto.request.RegisterRequest;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReportE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private TransactionService transactionService;

    static MySQLContainer<?> E2E_MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("e2edb")
            .withUsername("root")
            .withPassword("password");

    static GenericContainer<?> E2E_REDIS = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerMySQL(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", E2E_MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", E2E_MYSQL::getUsername);
        registry.add("spring.datasource.password", E2E_MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", E2E_MYSQL::getDriverClassName);

        registry.add("spring.data.redis.host", E2E_REDIS::getHost);
        registry.add("spring.data.redis.port", () -> E2E_REDIS.getMappedPort(6379));
    }

    static {
        E2E_MYSQL.start();
        E2E_REDIS.start();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        WebClient testServiceWebClient = webClientBuilder
                .baseUrl("http://localhost:" + port)
                .build();

        ReflectionTestUtils.setField(transactionService, "webClient", testServiceWebClient);
    }

    @AfterAll
    static void stop() {
        E2E_MYSQL.stop();
        E2E_REDIS.stop();
    }

    @Test
    void Report_E2E_Test() {
        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest("tester@email.com", "rawPassword", "테스터");

        given()
                .log().all()
                .contentType("application/json")
                .body(registerRequest)
                .when().post("/api/users")
                .then().log().all().statusCode(201);

        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest("tester@email.com", "rawPassword");

        String accessToken = given()
                .log().all()
                .contentType("application/json")
                .body(loginRequest)
                .when().post("/api/auth/login")
                .then().log().all().statusCode(200)
                .extract()
                .jsonPath()
                .getString("data.accessToken");

        // 3. 카드 등록 - 1
        RegisterCardRequest registerCardRequest = new RegisterCardRequest("HYUNDAI", "131324243535");

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(registerCardRequest)
                .when().post("/api/cards")
                .then().log().all().statusCode(201);

        // 4. 카드 등록 - 2
        RegisterCardRequest registerCardRequest2 = new RegisterCardRequest("SAMSUNG", "242457576868");

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(registerCardRequest2)
                .when().post("/api/cards")
                .then().log().all().statusCode(201);

        // 5. 카드 거래 데이터 삽입
        addCardTransactionData();

        // 6. 카드 거래 내역 동기화
        TransactionSyncRequest transactionSyncRequest = new TransactionSyncRequest(LocalDate.of(
                2025, 3, 1), LocalDate.of(2025, 8, 30));

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(transactionSyncRequest)
                .when().post("/api/transaction/sync")
                .then().log().all().statusCode(200);

        // 7. 수동 배치 실행
        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().post("/api/reports/run-report-batch")
                .then().log().all().statusCode(200);

        // 8. 리포트 알림 목록 조회
        ValidatableResponse response = given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/reports")
                .then().log().all().statusCode(200)
                .body("data.notificationInfoList[0].notificationMessage", equalTo("2025년 08월 리포트가 도착했습니다."));

        long reportId = response.extract().jsonPath().getLong("data.notificationInfoList[0].reportId");

        // 9. 리포트 상세 조회
        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/reports/" + reportId)
                .then().log().all().statusCode(200);
    }

    private void addCardTransactionData() {
        List<AddCardTransactionRequest> requests = List.of(
                // 7월
                // 현대카드 - 마트 7
                new AddCardTransactionRequest("test-merchant-1",
                        null, "HYUNDAI", "131324243535",
                        new BigDecimal("57500.00"), "홈플러스 방학점", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 7, 14, 15, 11), ZoneOffset.UTC), "APPROVED"),

                // 삼성카드 - 교통
                new AddCardTransactionRequest("test-merchant-2",
                        null, "SAMSUNG", "242457576868",
                        new BigDecimal("17450.00"), "카카오 택시", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 7, 28, 8, 15), ZoneOffset.UTC), "APPROVED"),

                // 현대카드 - 카페
                new AddCardTransactionRequest("test-merchant-3",
                        null, "HYUNDAI", "131324243535",
                        new BigDecimal("10500.00"), "스타벅스 강남점", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 7, 5, 6, 28), ZoneOffset.UTC), "APPROVED"),

                // 삼성카드 - 음식점
                new AddCardTransactionRequest("test-merchant-4",
                        null, "SAMSUNG", "242457576868",
                        new BigDecimal("248500.00"), "맥도날드", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 7, 28, 8, 15), ZoneOffset.UTC), "APPROVED"),

                // 8월
                // 현대카드 - 마트
                new AddCardTransactionRequest("test-merchant-5",
                        null, "HYUNDAI", "131324243535",
                        new BigDecimal("413500.00"), "이마트", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 8, 14, 15, 11), ZoneOffset.UTC), "APPROVED"),

                // 삼성카드 - 교통
                new AddCardTransactionRequest("test-merchant-6",
                        null, "SAMSUNG", "242457576868",
                        new BigDecimal("1400.00"), "버스", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 8, 28, 8, 15), ZoneOffset.UTC), "APPROVED"),

                // 현대카드 - 카페 승인
                new AddCardTransactionRequest("test-merchant-7",
                        null, "HYUNDAI", "131324243535",
                        new BigDecimal("10500.00"), "이디야 의정부점", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 8, 5, 6, 28), ZoneOffset.UTC), "APPROVED"),

                // 현대카드 - 카페 취소
                new AddCardTransactionRequest("test-merchant-8",
                        "test-merchant-7", "HYUNDAI", "131324243535",
                        new BigDecimal("10500.00"), "이디야 의정부점", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 8, 5, 6, 28), ZoneOffset.UTC), "CANCELED"),

                // 삼성카드 - 편의점
                new AddCardTransactionRequest("test-merchant-9",
                        null, "SAMSUNG", "242457576868",
                        new BigDecimal("4800.00"), "GS25", null,
                        OffsetDateTime.of(LocalDateTime.of(2025, 8, 28, 8, 15), ZoneOffset.UTC), "APPROVED")
        );

        for (AddCardTransactionRequest request : requests) {
            given().contentType("application/json")
                    .body(request)
                    .when().post("/outer/transaction")
                    .then().statusCode(201);
        }
    }
}
