package com.budget.ai.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    /** 사용자 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일 */
    @Column(nullable = false, unique = true)
    private String email;

    /** 비밀번호 */
    @Column(nullable = false)
    private String password;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 생성일시 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 삭제일시 */
    // Soft delete
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    /**
     * 비밀번호 변경
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * Soft delete 처리
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Soft delete 취소
     */
    public void cancelSoftDelete() {
        this.deletedAt = null;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
