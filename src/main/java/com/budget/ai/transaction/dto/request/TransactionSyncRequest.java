package com.budget.ai.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 카드 거래내역 동기화 요청 파라미터 DTO
 */
@Schema(description = "카드 거래내역 동기화 요청 파라미터 DTO")
public record TransactionSyncRequest(

        @Schema(description = "동기화 시작 날짜", example = "2025-03-20")
        @NotNull(message = "시작 날짜는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @Schema(description = "동기화 종료 날짜", example = "2025-03-20")
        @NotNull(message = "종료 날짜는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate
) {
}
