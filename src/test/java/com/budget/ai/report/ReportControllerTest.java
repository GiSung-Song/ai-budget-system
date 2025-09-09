package com.budget.ai.report;

import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.testsupport.ControllerTest;
import com.budget.ai.testsupport.TestAuthHelper;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest
class ReportControllerTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    private String accessToken;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
        TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

        accessToken = tokenResponse.accessToken();
    }

    @Nested
    class 리포트_알림_목록_조회_테스트 {

        private Report report1;
        private Report report2;
        private Report report3;

        @BeforeEach
        void setUpReport() {
            report1 = testDataFactory.createReport(user.getId(), LocalDate.of(2025, 4, 1),
                    "테스트 알림 - 1", "테스트 리포트 내용 - 1");

            report2 = testDataFactory.createReport(user.getId(), LocalDate.of(2025, 5, 1),
                    "테스트 알림 - 2", "테스트 리포트 내용 - 2");

            report3 = testDataFactory.createReport(user.getId(), LocalDate.of(2025, 3, 1),
                    "테스트 알림 - 3", "테스트 리포트 내용 - 3");
        }

        @Test
        void 알림_목록_최신순_조회_성공() throws Exception {
            mockMvc.perform(get("/api/reports")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.notificationInfoList[*].notificationMessage")
                            .value(contains("테스트 알림 - 2", "테스트 알림 - 1", "테스트 알림 - 3")));
        }

        @Test
        void 알림_목록_최신순_조회_비로그인_401반환() throws Exception {
            mockMvc.perform(get("/api/reports"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    class 리포트_조회_테스트 {
        private Report report;

        @BeforeEach
        void setUpReport() {
            report = testDataFactory.createReport(user.getId(), LocalDate.of(2025, 4, 1),
                    "테스트 알림 - 1", "테스트 리포트 내용 - 1");
        }

        @Test
        void 리포트_조회_성공() throws Exception {
            mockMvc.perform(get("/api/reports/{reportId}", report.getId())
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.reportMessage").value("테스트 리포트 내용 - 1"));
        }

        @Test
        void 리포트_조회_비로그인_401반환() throws Exception {
            mockMvc.perform(get("/api/reports/{reportId}", report.getId()))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        void 리포트_조회_없음_404반환() throws Exception {
            Report otherReport = testDataFactory.createReport(432132L, LocalDate.of(2025, 7, 1),
                    "테스트 알림 - 5", "테스트 리포트 내용 - 5");

            mockMvc.perform(get("/api/reports/{reportId}", otherReport.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}