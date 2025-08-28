package com.budget.ai.transaction;

import com.budget.ai.auth.CustomUserDetails;
import com.budget.ai.response.SuccessResponse;
import com.budget.ai.transaction.dto.request.TransactionQueryRequest;
import com.budget.ai.transaction.dto.request.TransactionSyncRequest;
import com.budget.ai.transaction.dto.response.SumCategoryTransactionResponse;
import com.budget.ai.transaction.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 거래 내역 관련 Controller
 * <p>
 *     거래내역 동기화, 거래내역 조회 기능 포함
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 거래 내역 동기화
     * @param userDetails 로그인한 회원 정보
     */
    @Operation(summary = "거래 내역 동기화", description = "거래 내역을 동기화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 내역 동기화 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @PostMapping("/sync")
    public ResponseEntity<SuccessResponse<Void>> syncTransaction(
            @Valid @RequestBody TransactionSyncRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        transactionService.syncTransaction(userDetails.id(), request);
        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 거래 내역 조회
     * @param userDetails 로그인한 회원 정보
     * @param request     조회 조건 및 필터링 조건
     * @return
     */
    @Operation(summary = "거래 내역 조회", description = "거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 내역 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<TransactionResponse>> getMyTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute TransactionQueryRequest request) {
        TransactionResponse response = transactionService.getTransaction(request, userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    /**
     * 카테고리별 거래 내역 통계 조회
     * @param userDetails 로그인한 회원 정보
     * @param startDate   조회 시작 날짜
     * @param endDate     조회 종료 날짜
     * @return
     */
    @Operation(summary = "카테고리별 거래 내역 통계 조회", description = "카테고리별 거래 내역 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 거래 내역 통계 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @GetMapping("/summary")
    public ResponseEntity<SuccessResponse<SumCategoryTransactionResponse>> getMyTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        SumCategoryTransactionResponse result = transactionService.getSumCategoryTransaction(userDetails.id(), start, end);

        return ResponseEntity.ok(SuccessResponse.of(result));
    }
}