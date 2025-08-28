package com.budget.ai.transaction;

import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.transaction.dto.request.TransactionSyncRequest;
import com.budget.ai.user.dto.request.RegisterRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
public class TransactionE2ETest {

    static MySQLContainer<?> E2E_MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("e2edb")
            .withUsername("root")
            .withPassword("password");

    static GenericContainer<?> E2E_REDIS = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    static {
        E2E_MYSQL.start();
        E2E_REDIS.start();
    }

    @DynamicPropertySource
    static void registerMySQL(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", E2E_MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", E2E_MYSQL::getUsername);
        registry.add("spring.datasource.password", E2E_MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", E2E_MYSQL::getDriverClassName);

        registry.add("spring.data.redis.host", E2E_REDIS::getHost);
        registry.add("spring.data.redis.port", () -> E2E_REDIS.getMappedPort(6379));
    }

    @LocalServerPort
    int port;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @AfterAll
    static void cleanUp() {
        E2E_MYSQL.stop();
        E2E_REDIS.stop();
    }

    @Test
    void TRANSACTION_E2E_테스트() {
        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest("tester@email.com", "rawPassword", "테스터");

        given()
                .log().all()
                .contentType("application/json")
                .body(registerRequest)
                .when().post(baseUrl() + "/api/users")
                .then().log().all().statusCode(201);

        // 2. 로그인 + 토큰 획득
        LoginRequest loginRequest = new LoginRequest("tester@email.com", "rawPassword");

        String accessToken = given()
                .log().all()
                .contentType("application/json")
                .body(loginRequest)
                .when().post(baseUrl() + "/api/auth/login")
                .then().log().all().statusCode(200)
                .extract()
                .jsonPath()
                .getString("data.accessToken");

        // 3. 카드 등록
        RegisterCardRequest registerCardRequest = new RegisterCardRequest("HYUNDAI", "131324243535");

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(registerCardRequest)
                .when().post(baseUrl() + "/api/cards")
                .then().log().all().statusCode(201);

        // 3-1. 두 번째 카드 등록
        RegisterCardRequest registerCardRequest2 = new RegisterCardRequest("SAMSUNG", "242457576868");

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(registerCardRequest2)
                .when().post(baseUrl() + "/api/cards")
                .then().log().all().statusCode(201);

        // 카드 거래내역 데이터 삽입 (가짜 데이터 테스트용)
        addCardTransactionData();

        // 4. 거래내역 동기화 (카드 거래내역 API 호출 및 OpenAI API 호출)
        TransactionSyncRequest transactionSyncRequest = new TransactionSyncRequest(LocalDate.of(
                2025, 3, 1), LocalDate.of(2025, 8, 30));

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(transactionSyncRequest)
                .when().post(baseUrl() + "/api/transaction/sync")
                .then().log().all().statusCode(200);

        // 4-1. 거래내역 동기화 2번째 중복된 데이터 조회 시 중복저장 하지 않는지 체크하기 위한 테스트(API 호출 X)
        TransactionSyncRequest transactionSyncRequest2 = new TransactionSyncRequest(LocalDate.of(
                2025, 8, 1), LocalDate.of(2025, 8, 30));

        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .body(transactionSyncRequest2)
                .when().post(baseUrl() + "/api/transaction/sync")
                .then().log().all().statusCode(200);

        // 5. 거래내역 조회
        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get(baseUrl() + "/api/transaction?page=0&size=10&startDate=2025-07-01&endDate=2025-08-30")
                .then().log().all()
                .statusCode(200)
                .body("data.transactionList.size()", equalTo(6));

        // 6. 거래내역 통계 조회
        float totalSumFloat = given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get(baseUrl() + "/api/transaction/summary?startDate=2025-07-01&endDate=2025-12-12")
                .then().log().all()
                .statusCode(200)
                .body("data.sumCategoryInfoList[0].categoryName", equalTo("교통"))
                .body("data.sumCategoryInfoList[1].categoryName", equalTo("마트"))
                .body("data.sumCategoryInfoList[2].categoryName", equalTo("문화"))
                .body("data.sumCategoryInfoList[3].categoryName", equalTo("음식점"))
                .extract().jsonPath().getFloat("data.totalSum");

        BigDecimal actual = new BigDecimal(Float.toString(totalSumFloat));
        BigDecimal expected = new BigDecimal("351500.0");

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private void addCardTransactionData() {
        AddCardTransactionRequest cardTransaction1 = new AddCardTransactionRequest("test-merchant-1",
                null, "HYUNDAI", "131324243535",
                new BigDecimal("57500.00"), "홈플러스 방학점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 7, 14, 15, 11), ZoneOffset.UTC), "APPROVED");

        AddCardTransactionRequest cardTransaction2 = new AddCardTransactionRequest("test-merchant-2",
                null, "HYUNDAI", "131324243535",
                new BigDecimal("13500.00"), "이디야 신촌점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 8, 20, 23, 22), ZoneOffset.UTC), "APPROVED");

        AddCardTransactionRequest cardTransaction3 = new AddCardTransactionRequest("test-merchant-3",
                "test-merchant-2", "HYUNDAI", "131324243535",
                new BigDecimal("13500.00"), "이디야 신촌점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 8, 20, 23, 23), ZoneOffset.UTC), "CANCELED");

        AddCardTransactionRequest cardTransaction4 = new AddCardTransactionRequest("test-merchant-4",
                null, "SAMSUNG", "242457576868",
                new BigDecimal("248500.00"), "카카오 택시", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 7, 28, 8, 15), ZoneOffset.UTC), "APPROVED");

        AddCardTransactionRequest cardTransaction5 = new AddCardTransactionRequest("test-merchant-5",
                null, "SAMSUNG", "242457576868",
                new BigDecimal("17500.00"), "롯데리아 방학점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 8, 26, 15, 28), ZoneOffset.UTC), "APPROVED");

        AddCardTransactionRequest cardTransaction6 = new AddCardTransactionRequest("test-merchant-6",
                null, "SAMSUNG", "242457576868",
                new BigDecimal("28000.00"), "메가박스 창동점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 8, 3, 14, 20), ZoneOffset.UTC), "APPROVED");

        given().contentType("application/json")
                .body(cardTransaction1)
                .when().post(baseUrl() + "/outer/transaction")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(cardTransaction2)
                .when().post(baseUrl() + "/outer/transaction")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(cardTransaction3)
                .when().post(baseUrl() + "/outer/transaction")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(cardTransaction4)
                .when().post(baseUrl() + "/outer/transaction")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(cardTransaction5)
                .when().post(baseUrl() + "/outer/transaction")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(cardTransaction6)
                .when().post(baseUrl() + "/outer/transaction")
                .then().statusCode(201);
    }
}