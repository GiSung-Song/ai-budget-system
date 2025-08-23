package com.budget.ai.external.transaction;

import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.external.transaction.dto.response.CardTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * 카드 거래 내역 API Controller
 * <p>
 *     가짜 카드 거래 내역 추가, 가짜 카드 거래 내역 조회 기능 포함
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/outer/transaction")
public class CardTransactionController {

    private final CardTransactionService cardTransactionService;

    @Operation(summary = "카드 거래 내역 추가", description = "카드 거래 내역을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카드 거래 내역 추가 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카드"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 거래 내역"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addCardTransaction(@RequestBody @Valid AddCardTransactionRequest request) {
        cardTransactionService.addCardTransaction(request);
    }

    @Operation(summary = "카드 거래 내역 조회", description = "카드 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 거래 내역 조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @GetMapping
    public ResponseEntity<CardTransactionResponse> addCardTransaction(
            @RequestParam String startDate,
            @RequestParam String cardNumber) {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse(startDate);
        CardTransactionResponse cardTransactionList = cardTransactionService.getCardTransactionList(offsetDateTime, cardNumber);

        return ResponseEntity.ok(cardTransactionList);
    }
}
