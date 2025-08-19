package com.budget.ai.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 카드 JPA Repository
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * 카드 등록 여부 체크
     * @param cardCompanyType 카드사
     * @param cardNumber      카드번호
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByCardCompanyTypeAndCardNumber(CardCompanyType cardCompanyType, String cardNumber);

    /**
     * 카드 조회
     * @param cardId 카드 고유 식별자
     * @param userId 회원 고유 식별자
     * @return 존재하면 Card
     */
    Optional<Card> findByIdAndUserId(Long cardId, Long userId);

    /**
     * 카드 목록 조회
     * @param userId 회원 고유 식별자
     * @return Card List
     */
    List<Card> findAllByUserId(Long userId);

    /**
     * 테스트용 카드 조회
     * @param cardCompanyType 카드사
     * @param cardNumber      카드번호
     * @param userId          회원 고유 식별자
     * @return Card
     */
    Optional<Card> findByCardCompanyTypeAndCardNumberAndUserId(CardCompanyType cardCompanyType, String cardNumber, Long userId);
}