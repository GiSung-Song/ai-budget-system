package com.budget.ai.report;

import com.budget.ai.report.dto.response.NotificationInfoDto;
import com.budget.ai.report.dto.response.NotificationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT new com.budget.ai.report.dto.response.NotificationInfoDto(r.id, r.notificationMessage) " +
            "FROM Report r WHERE r.userId = :userId ORDER BY r.reportMonth DESC")
    List<NotificationInfoDto> findAllNotificationByUserId(@Param("userId") Long userId);

    @Query("SELECT r.reportMessage FROM Report r WHERE r.id = :reportId AND r.userId = :userId")
    Optional<String> findReportMessageByIdAndUserId(@Param("reportId") Long reportId, @Param("userId") Long userId);
}