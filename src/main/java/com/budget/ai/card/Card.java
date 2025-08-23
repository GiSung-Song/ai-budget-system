package com.budget.ai.card;

import com.budget.ai.config.BaseTimeEntity;
import com.budget.ai.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 카드 엔티티
 */
@Entity
@Table(name = "cards",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_card_company_number", columnNames = {"card_company_type", "card_number"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends BaseTimeEntity {

    /** 카드 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카드사 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardCompanyType cardCompanyType;

    /** 카드 번호 */
    @Column(nullable = false, length = 20)
    private String cardNumber;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 마지막 동기화 시간 */
    @Column(name = "synchronized_at")
    private LocalDateTime synchronizedAt;

    @Builder
    public Card(CardCompanyType cardCompanyType, String cardNumber, User user) {
        this.cardCompanyType = cardCompanyType;
        this.cardNumber = cardNumber;
        this.user = user;
    }

    public void updateSyncTime(OffsetDateTime syncTime) {
        this.synchronizedAt = syncTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
