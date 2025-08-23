package com.budget.ai.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 거래 타입
 * <p>
 *     결제, 출금, 예금, 이체
 * </p>
 */
@RequiredArgsConstructor
@Getter
public enum TransactionType {
    PAYMENT("결제"),
    WITHDRAW("출금"),
    DEPOSIT("예금"),
    TRANSFER("이체");

    private final String displayName;
}