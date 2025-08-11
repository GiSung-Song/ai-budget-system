package com.budget.ai.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 관련 JPA Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 회원 조회
     * @param email 이메일
     * @return 존재하면 회원 반환
     */
    Optional<User> findByEmail(String email);

    /**
     * 이미 존재하는 이메일인지 조회
     * @param email 이메일
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByEmail(String email);

    /**
     * 이름과 이메일로 탈퇴된 회원 조회
     * @param name  이름
     * @param email 이메일
     * @return 존재하면 회원 반환
     */
    Optional<User> findByNameAndEmailAndDeletedAtIsNotNull(String name, String email);
}