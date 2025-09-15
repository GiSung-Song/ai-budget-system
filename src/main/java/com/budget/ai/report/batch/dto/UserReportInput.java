package com.budget.ai.report.batch.dto;

import com.budget.ai.transaction.dto.TransactionReportDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 배치용 사용자-카테고리별 합계 DTO
 */
@Schema(description = "배치용 사용자-카테고리별 합계 DTO")
public record UserReportInput(
        @Schema(description = "사용자 식별자 ID")
        Long userId,

        @Schema(description = "거래내역")
        List<TransactionReportDto> transactions
) {
}
