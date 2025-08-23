package com.budget.ai.external.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카드 거래 Jpa Repository
 */
public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {

    boolean existsByMerchantIdAndCardNumber(String merchantId, String cardNumber);
    List<CardTransaction> findByCardNumberAndTransactionAtAfter(String cardNumber, LocalDateTime transactionAt);
}