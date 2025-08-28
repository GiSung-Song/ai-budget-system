package com.budget.ai.transaction;

import com.budget.ai.card.Card;
import com.budget.ai.category.Category;
import com.budget.ai.config.BaseTimeEntity;
import com.budget.ai.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래내역 Entity
 */
@Entity
@Table(name = "transactions",
        uniqueConstraints = @UniqueConstraint(name = "uq_txn", columnNames = {"card_id", "merchant_id", "transaction_at"}),
        indexes = {
                @Index(name = "idx_txn_time", columnList = "card_id, merchant_id, transaction_at"),
                @Index(name = "idx_original_merchant", columnList = "original_merchant_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Transaction extends BaseTimeEntity {

    /**
     * 거래 고유 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 카드
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /**
     * 카테고리
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * 카드사 거래 고유 ID
     */
    @Column(nullable = false)
    private String merchantId;

    /**
     * 환불/취소 시 참조하는 카드사 거래 고유 ID
     */
    private String originalMerchantId;

    /**
     * 금액
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * 가게명
     */
    @Column(nullable = false)
    private String merchantName;

    /**
     * 가게 주소
     */
    private String merchantAddress;

    /**
     * 거래 시각
     */
    @Column(nullable = false)
    private LocalDateTime transactionAt;

    /**
     * 거래 상태 (APPROVED, CANCELED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;
}
