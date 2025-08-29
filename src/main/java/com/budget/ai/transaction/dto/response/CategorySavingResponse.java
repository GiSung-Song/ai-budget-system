package com.budget.ai.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 카테고리별 절약 방법 조회 응답 DTO
 */
@Schema(description = "카테고리별 절약 방법 조회 응답 DTO")
public record CategorySavingResponse(
        @Schema(description = "카테고리별 절약 정보")
        List<SavingInfo> savingInfoList
) {
        public record SavingInfo(
                @Schema(description = "카테고리 이름", example = "교통")
                String categoryName,

                @Schema(description = "추천 절약 방법", example = "택시를 조금만 타세요.")
                String recommendation
        ) { }
}