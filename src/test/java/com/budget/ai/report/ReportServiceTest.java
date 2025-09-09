package com.budget.ai.report;

import com.budget.ai.report.dto.response.NotificationResponse;
import com.budget.ai.report.dto.response.ReportResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.testsupport.ServiceTest;
import com.budget.ai.testsupport.container.TestContainerManager;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ServiceTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    Report report1;
    Report report2;
    Report report3;

    @BeforeEach
    void setUp() {
        report1 = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 5, 1))
                .reportMessage("테스트 리포트 - 1 내용입니다.")
                .notificationMessage("테스트 알림 - 1 메시지입니다.")
                .build();

        report2 = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 4, 1))
                .reportMessage("테스트 리포트 - 2 내용입니다.")
                .notificationMessage("테스트 알림 - 2 메시지입니다.")
                .build();

        report3 = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 3, 1))
                .reportMessage("테스트 리포트 - 3 내용입니다.")
                .notificationMessage("테스트 알림 - 3 메시지입니다.")
                .build();

        reportRepository.saveAll(List.of(report1, report2, report3));
    }

    @Test
    void 알림_목록_조회() {
        NotificationResponse notificationList = reportService.getNotificationList(1L);

        assertThat(notificationList.notificationInfoList())
                .hasSize(3)
                .extracting(
                        NotificationResponse.NotificationInfo::reportId,
                        NotificationResponse.NotificationInfo::notificationMessage
                )
                .containsExactly(
                        Tuple.tuple(
                                report1.getId(),
                                report1.getNotificationMessage()
                        ),
                        Tuple.tuple(
                                report2.getId(),
                                report2.getNotificationMessage()
                        ),
                        Tuple.tuple(
                                report3.getId(),
                                report3.getNotificationMessage()
                        )
                );
    }

    @Test
    void 리포트_메시지_조회_존재() {
        ReportResponse report = reportService.getReport(1L, report1.getId());

        assertThat(report.reportMessage()).isEqualTo(report1.getReportMessage());
    }

    @Test
    void 리포트_메시지_존재하지_않으면_404반환() {
        assertThatThrownBy(() -> reportService.getReport(3L, report1.getId()))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException exception = (CustomException) ex;

                    assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REPORT_NOT_FOUND);
                });
    }
}