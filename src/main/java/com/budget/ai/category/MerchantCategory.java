package com.budget.ai.category;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매장과 카테고리 매핑 엔티티
 */
@Entity
@Table(name = "merchant_categories",
        uniqueConstraints = @UniqueConstraint(name = "uq_merchant_name", columnNames = "merchant_name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantCategory {

    /** 매장 카테고리 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 매장 이름 패턴 */
    @Column(nullable = false, length = 100)
    private String merchantName;
}
