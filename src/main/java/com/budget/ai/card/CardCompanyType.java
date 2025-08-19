package com.budget.ai.card;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 카드사 종류
 */
@Getter
@RequiredArgsConstructor
public enum CardCompanyType {
    SHINHAN("신한카드"),
    SAMSUNG("삼성카드"),
    KAKAO("카카오카드"),
    WOORI("우리카드"),
    KB("국민카드"),
    HANA("하나카드"),
    HYUNDAI("현대카드"),
    NH("농협카드"),
    ;

    private final String displayName;
}
