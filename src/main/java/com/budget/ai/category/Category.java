package com.budget.ai.category;

import com.budget.ai.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 카테고리 엔티티
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    /** 카테고리 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리 코드 */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 카테고리 이름 */
    @Column(nullable = false, length = 100)
    private String displayName;
}
