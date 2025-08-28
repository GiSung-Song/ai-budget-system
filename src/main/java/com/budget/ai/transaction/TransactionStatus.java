package com.budget.ai.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 거래 상태
 * <p>
 *     승인, 환불, 취소
 * </p>
 */
@RequiredArgsConstructor
@Getter
public enum TransactionStatus {
    APPROVED("승인"),
    CANCELED("취소");

    private final String displayName;
}
