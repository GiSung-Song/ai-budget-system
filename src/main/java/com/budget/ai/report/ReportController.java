package com.budget.ai.report;

import com.budget.ai.auth.CustomUserDetails;
import com.budget.ai.report.dto.response.NotificationResponse;
import com.budget.ai.report.dto.response.ReportResponse;
import com.budget.ai.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 리포트 관련 Controller
 * <p>
 *     리포트 상세 조회, 리포트 알림 목록 조회 기능 포함
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 리포트 알림 목록 조회
     * @param userDetails 로그인한 사용자 정보
     * @return 리포트 알림 목록 (최신순)
     */
    @Operation(summary = "리포트 알림 목록 조회", description = "리포트 알림 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리포트 알림 목록 조회"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<NotificationResponse>> getAllReportNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        NotificationResponse notificationList = reportService.getNotificationList(userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of(notificationList));
    }

    /**
     * 리포트 상세 조회
     * @param userDetails 로그인한 사용자 정보
     * @param reportId    리포트 식별자 ID
     * @return 전달과 전전달 카테고리별 금액 비교 리포트 상세 조회
     */
    @Operation(summary = "리포트 조회", description = "리포트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리포트 조회"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리포트"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{reportId}")
    public ResponseEntity<SuccessResponse<ReportResponse>> getReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("reportId") Long reportId
    ) {
        ReportResponse report = reportService.getReport(userDetails.id(), reportId);

        return ResponseEntity.ok(SuccessResponse.of(report));
    }

    @Operation(summary = "리포트 배치 실행", description = "리포트 배치를 실행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리포트 배치 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/run-report-batch")
    public ResponseEntity<SuccessResponse<Void>> runReportBatch() {
        reportService.runBatchJob();

        return ResponseEntity.ok(SuccessResponse.of());
    }
}
