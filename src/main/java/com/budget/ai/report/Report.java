package com.budget.ai.report;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = @UniqueConstraint(name = "uq_reports_user_month", columnNames = {"user_id", "report_month"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "report_month", nullable = false)
    private LocalDate reportMonth;

    @Column(name = "report_message", nullable = false, columnDefinition = "TEXT")
    private String reportMessage;

    @Column(name = "notification_message", nullable = false, columnDefinition = "TEXT")
    private String notificationMessage;
}