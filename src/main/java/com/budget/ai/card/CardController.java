package com.budget.ai.card;

import com.budget.ai.auth.CustomUserDetails;
import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.card.dto.response.CardInfoResponse;
import com.budget.ai.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 카드 관련 Controller
 * <p>
 * 카드 등록, 카드 삭제, 카드 목록 조회 기능 포함
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "카드 등록", description = "카드를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카드 등록 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 카드"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid RegisterCardRequest request) {

        cardService.registerCard(userDetails.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of());
    }

    @Operation(summary = "카드 삭제", description = "카드를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 경로 변수"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카드"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<SuccessResponse<Void>> deleteCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        cardService.deleteCard(userDetails.id(), cardId);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    @Operation(summary = "카드 목록 조회", description = "카드 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @GetMapping
    public ResponseEntity<SuccessResponse<CardInfoResponse>> getMyCardInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CardInfoResponse myCardInfo = cardService.getMyCardInfo(userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of(myCardInfo));
    }
}
