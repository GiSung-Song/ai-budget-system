package com.budget.ai.card.dto.response;

import com.budget.ai.card.Card;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카드 정보 조회 응답 DTO
 */
@Schema(description = "카드 정보 조회 응답 DTO")
public record CardInfoResponse(
        @Schema(description = "카드 정보 목록")
        List<CardInfo> cardList
) {
    public record CardInfo(
            @Schema(description = "카드 고유 식별자")
            Long cardId,

            @Schema(description = "카드사")
            String cardCompanyType,

            @Schema(description = "카드 번호")
            String cardNumber,

            @Schema(description = "등록일자")
            LocalDateTime createdAt
    ) {
        public static CardInfo from(Card card) {
            return new CardInfo(
                    card.getId(),
                    card.getCardCompanyType().getDisplayName(),
                    card.getCardNumber(),
                    card.getCreatedAt()
            );
        }
    }
}
