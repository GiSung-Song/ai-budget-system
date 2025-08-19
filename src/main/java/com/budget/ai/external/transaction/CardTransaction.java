package com.budget.ai.external.transaction;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 가짜 카드 거래내역 엔티티
 */
@Entity
@Table(name = "card_transaction",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_card_merchant", columnNames = {"merchant_id", "card_number"})
        },
        indexes = {
                @Index(name = "idx_card_txn_time", columnList = "card_number, transaction_at"),
                @Index(name = "idx_original_merchant", columnList = "original_merchant_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카드사 거래 고유 ID */
    @Column(nullable = false)
    private String merchantId;

    /** 환불/취소 시 참조하는 카드사 거래 고유 ID */
    private String originalMerchantId;

    /** 카드 번호 */
    @Column(nullable = false)
    private String cardNumber;

    /** 금액 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** 가게명 */
    @Column(nullable = false)
    private String merchantName;

    /** 가게 주소 */
    private String merchantAddress;

    /** 거래 시각 */
    @Column(nullable = false)
    private LocalDateTime transactionAt;

    /** 거래 유형 (PAYMENT, WITHDRAW, DEPOSIT, TRANSFER) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardTransactionType cardTransactionType;

    /** 거래 상태 (APPROVED, REFUND, CANCELED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardTransactionStatus cardTransactionStatus;

    @Builder
    public CardTransaction(String merchantId,
                           String originalMerchantId,
                           String cardNumber,
                           BigDecimal amount,
                           String merchantName,
                           String merchantAddress,
                           LocalDateTime transactionAt,
                           CardTransactionType cardTransactionType,
                           CardTransactionStatus cardTransactionStatus) {
        this.merchantId = merchantId;
        this.originalMerchantId = originalMerchantId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.merchantName = merchantName;
        this.merchantAddress = merchantAddress;
        this.transactionAt = transactionAt;
        this.cardTransactionType = cardTransactionType;
        this.cardTransactionStatus = cardTransactionStatus;
    }
}
