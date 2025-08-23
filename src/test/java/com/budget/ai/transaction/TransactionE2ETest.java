package com.budget.ai.transaction;

import com.budget.ai.auth.dto.request.LoginRequest;
import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
public class TransactionE2ETest {

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        TestContainerManager.startRedis();

        TestContainerManager.registerMySQL(registry);
        TestContainerManager.registerRedis(registry);
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void MVP_E2E_테스트() {
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

        // 카드 거래내역 데이터 삽입 (가짜 데이터 테스트용)
        addCardTransactionData();

        // 4. 거래내역 동기화 (카드 거래내역 API 호출 및 OpenAI API 호출)
        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().post(baseUrl() + "/api/transaction/sync")
                .then().log().all().statusCode(200);

        // 6. 거래내역 조회
        given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get(baseUrl() + "/api/transaction?page=0&size=10&startDate=2025-07-01&endDate=2025-08-25")
                .then().log().all()
                .statusCode(200)
                .body("data.transactionList.size()", equalTo(2))
                .body("data.transactionList[0].merchantName", equalTo("이디야 신촌점"))
                .body("data.transactionList[0].categoryName", equalTo("카페"));

    }

    private void addCardTransactionData() {
        AddCardTransactionRequest cardTransaction1 = new AddCardTransactionRequest("test-merchant-1",
                null, "HYUNDAI", "131324243535",
                new BigDecimal("57500.00"), "홈플러스 방학점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 7, 14, 15, 11), ZoneOffset.UTC),
                "PAYMENT", "APPROVED");

        AddCardTransactionRequest cardTransaction2 = new AddCardTransactionRequest("test-merchant-2",
                null, "HYUNDAI", "131324243535",
                new BigDecimal("13500.00"), "이디야 신촌점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 8, 20, 23, 22), ZoneOffset.UTC),
                "PAYMENT", "APPROVED");

        AddCardTransactionRequest cardTransaction3 = new AddCardTransactionRequest("test-merchant-3",
                "test-merchant-2", "HYUNDAI", "131324243535",
                new BigDecimal("13500.00"), "이디야 신촌점", null,
                OffsetDateTime.of(LocalDateTime.of(2025, 8, 20, 23, 23), ZoneOffset.UTC),
                "PAYMENT", "CANCELED");

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
    }
}
