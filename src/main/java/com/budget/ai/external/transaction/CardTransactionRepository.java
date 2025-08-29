package com.budget.ai.external.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카드 거래 Jpa Repository
 */
public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {

    boolean existsByMerchantIdAndCardNumber(String merchantId, String cardNumber);

    @Query("""
        SELECT ct
        FROM CardTransaction ct
        WHERE ct.cardNumber = :cardNumber
        AND ct.transactionAt >= :startDate
        AND ct.transactionAt < :endDate
    """)
    List<CardTransaction> findByCardNumberAndBetweenDate(
            @Param("cardNumber") String cardNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}