package com.budget.ai.report;

import com.budget.ai.report.dto.response.NotificationInfoDto;
import com.budget.ai.report.dto.response.NotificationResponse;
import com.budget.ai.testsupport.RepositoryTest;
import com.budget.ai.testsupport.container.TestContainerManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Test
    void 모든_리포트_알림_목록_조회() {
        Report report1 = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 5, 1))
                .reportMessage("테스트 리포트 - 1 내용입니다.")
                .notificationMessage("테스트 알림 - 1 메시지입니다.")
                .build();

        Report report2 = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 4, 1))
                .reportMessage("테스트 리포트 - 2 내용입니다.")
                .notificationMessage("테스트 알림 - 2 메시지입니다.")
                .build();

        Report report3 = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 3, 1))
                .reportMessage("테스트 리포트 - 3 내용입니다.")
                .notificationMessage("테스트 알림 - 3 메시지입니다.")
                .build();

        reportRepository.saveAll(List.of(report1, report2, report3));

        List<NotificationInfoDto> notificationInfoList = reportRepository.findAllNotificationByUserId(1L);

        assertThat(notificationInfoList)
                .hasSize(3)
                .extracting(
                        NotificationInfoDto::notificationMessage
                )
                .containsExactly(
                        report1.getNotificationMessage(),
                        report2.getNotificationMessage(),
                        report3.getNotificationMessage()
                );
    }

    @Test
    void 리포트_메시지_조회_존재() {
        Report report = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 5, 1))
                .reportMessage("테스트 리포트 - 1 내용입니다.")
                .notificationMessage("테스트 알림 - 1 메시지입니다.")
                .build();

        reportRepository.save(report);

        String reportMessage = reportRepository.findReportMessageByIdAndUserId(report.getId(), 1L)
                .orElseThrow();

        assertThat(reportMessage).isEqualTo(report.getReportMessage());
    }

    @Test
    void 리포트_메시지_조회_없음() {
        Report report = Report.builder()
                .userId(1L)
                .reportMonth(LocalDate.of(2025, 5, 1))
                .reportMessage("테스트 리포트 - 1 내용입니다.")
                .notificationMessage("테스트 알림 - 1 메시지입니다.")
                .build();

        reportRepository.save(report);

        String reportMessage = reportRepository.findReportMessageByIdAndUserId(report.getId(), 6L)
                .orElse(null);

        assertThat(reportMessage).isNull();
    }
}