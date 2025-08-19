package com.budget.ai.external.transaction;

import com.budget.ai.testsupport.RepositoryTest;
import com.budget.ai.testsupport.container.TestContainerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class CardTransactionRepositoryTest {

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private static final String TEST_CARD_NUMBER = "123412341234";

    @BeforeEach
    void setUp() {
        List<CardTransaction> testData = List.of(
                CardTransaction.builder()
                        .merchantId("test-merchant-1")
                        .cardNumber(TEST_CARD_NUMBER)
                        .amount(new BigDecimal("12500.00"))
                        .merchantName("스타벅스 강남점")
                        .transactionAt(OffsetDateTime.parse("2025-08-15T13:05:03+09:00")
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toLocalDateTime())
                        .cardTransactionType(CardTransactionType.PAYMENT)
                        .cardTransactionStatus(CardTransactionStatus.APPROVED)
                        .build(),

                CardTransaction.builder()
                        .merchantId("test-merchant-2")
                        .cardNumber(TEST_CARD_NUMBER)
                        .amount(new BigDecimal("37510.00"))
                        .merchantName("KFC 서초점")
                        .transactionAt(OffsetDateTime.parse("2025-08-17T23:12:11+09:00")
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toLocalDateTime())
                        .cardTransactionType(CardTransactionType.PAYMENT)
                        .cardTransactionStatus(CardTransactionStatus.APPROVED)
                        .build()
                );

        cardTransactionRepository.saveAll(testData);
    }

    @Test
    void 카드번호와_거래번호_중복_시_true_반환() {
        boolean exists = cardTransactionRepository.existsByMerchantIdAndCardNumber(
                "test-merchant-1", TEST_CARD_NUMBER);

        assertThat(exists).isTrue();
    }

    @Test
    void 카드번호와_거래번호_중복_아닐_시_false_반환() {
        boolean exists = cardTransactionRepository.existsByMerchantIdAndCardNumber(
                "test-merchant-3", TEST_CARD_NUMBER);

        assertThat(exists).isFalse();
    }

    @Test
    void 카드_번호_없음_빈_리스트_반환() {
        List<CardTransaction> result = cardTransactionRepository.findByCardNumberAndTransactionAtAfter(
                "00000000", LocalDateTime.of(2025, 8, 14, 0, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void 특정_날짜_이후_거래내역_없음_빈_리스트_반환() {
        LocalDateTime dateTime = OffsetDateTime.parse("2025-08-20T13:00:00+09:00")
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        List<CardTransaction> result = cardTransactionRepository.findByCardNumberAndTransactionAtAfter(
                TEST_CARD_NUMBER, dateTime);

        assertThat(result).isEmpty();
    }

    @Test
    void 특정_날짜_이후_거래내역_단건_반환() {
        LocalDateTime dateTime = OffsetDateTime.parse("2025-08-17T13:00:00+09:00")
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        List<CardTransaction> result = cardTransactionRepository.findByCardNumberAndTransactionAtAfter(
                TEST_CARD_NUMBER, dateTime);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMerchantId())
                .isEqualTo("test-merchant-2");
    }

    @Test
    void 특정_날짜_이후_거래내역_모두_반환() {
        LocalDateTime dateTime = OffsetDateTime.parse("2025-08-10T13:00:00+09:00")
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        List<CardTransaction> result = cardTransactionRepository.findByCardNumberAndTransactionAtAfter(
                TEST_CARD_NUMBER, dateTime);

        assertThat(result).hasSize(2);
    }
}