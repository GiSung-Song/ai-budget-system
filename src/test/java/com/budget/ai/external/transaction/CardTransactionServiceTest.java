package com.budget.ai.external.transaction;

import com.budget.ai.card.Card;
import com.budget.ai.card.CardCompanyType;
import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.external.transaction.dto.response.CardTransactionResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.testsupport.ServiceTest;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ServiceTest
class CardTransactionServiceTest {

    @Autowired
    private CardTransactionService cardTransactionService;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
        card = testDataFactory.createCard(CardCompanyType.HYUNDAI, "123412341234", user);
    }

    @Nested
    class 카드_거래내역_추가_테스트 {

        @Test
        void 카드_거래내역_추가_정상() {
            AddCardTransactionRequest request = new AddCardTransactionRequest(
                    "test-merchant-1",
                    null,
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234",
                    new BigDecimal("17500.00"),
                    "흥부 부대찌개 도봉점",
                    null,
                    OffsetDateTime.parse("2025-07-25T04:18:12+09:00"),
                    CardTransactionStatus.APPROVED.name()
            );

            cardTransactionService.addCardTransaction(request);

            List<CardTransaction> result = cardTransactionRepository.findAll();

            assertThat(result).hasSize(1);

            CardTransaction cardTransaction = result.get(0);

            assertThat(cardTransaction.getMerchantId()).isEqualTo(request.merchantId());

            assertThat(cardTransaction.getTransactionAt()).isEqualTo(
                    request.transactionAt()
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .toLocalDateTime()
            );
        }

        @Test
        void 중복_거래_409반환() {
            CardTransaction cardTransaction = testDataFactory.createCardTransaction(
                    "test-merchant-1",
                    "123412341234",
                    "50000.00",
                    "맥도날드 방학점",
                    "2025-08-15T03:22:32+09:00",
                    CardTransactionStatus.APPROVED
            );

            AddCardTransactionRequest request = new AddCardTransactionRequest(
                    "test-merchant-1",
                    null,
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234",
                    new BigDecimal("17500.00"),
                    "흥부 부대찌개 도봉점",
                    null,
                    OffsetDateTime.parse("2025-07-25T04:18:12+09:00"),
                    CardTransactionStatus.APPROVED.name()
            );

            assertThatThrownBy(() -> cardTransactionService.addCardTransaction(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CARD_TRANSACTION_ALREADY_EXISTS);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        void 카드_없음_404반환() {
            AddCardTransactionRequest request = new AddCardTransactionRequest(
                    "test-merchant-1",
                    null,
                    CardCompanyType.HYUNDAI.name(),
                    "123412341235",
                    new BigDecimal("17500.00"),
                    "흥부 부대찌개 도봉점",
                    null,
                    OffsetDateTime.parse("2025-07-25T04:18:12+09:00"),
                    CardTransactionStatus.APPROVED.name()
            );

            assertThatThrownBy(() -> cardTransactionService.addCardTransaction(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CARD_NOT_FOUND);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 거래내역_조회2_테스트 {

        private CardTransaction ct1;
        private CardTransaction ct2;
        private CardTransaction ct3;

        @BeforeEach
        void setUp() {
            ct1 = testDataFactory.createCardTransaction(
                    "test-merchant-1",
                    "123412341234",
                    "13500.00",
                    "KFC 방학점",
                    "2025-08-10T05:10:15+09:00",
                    CardTransactionStatus.APPROVED
            );

            ct2 = testDataFactory.createCardTransaction(
                    "test-merchant-2",
                    "123412341234",
                    "29000.00",
                    "롯데리아 방학점",
                    "2025-08-12T21:20:58+09:00",
                    CardTransactionStatus.APPROVED
            );

            ct3 = testDataFactory.createCardTransaction(
                    "test-merchant-3",
                    "123412341234",
                    "38050.00",
                    "맥도날드 방학점",
                    "2025-08-14T15:38:04+09:00",
                    CardTransactionStatus.APPROVED
            );
        }

        @Test
        void 거래내역_0건() {
            OffsetDateTime startDate = OffsetDateTime.parse("2025-08-17T00:00:00+09:00");
            OffsetDateTime endDate = OffsetDateTime.parse("2025-08-20T00:00:00+09:00");
            String cardNumber = "123412341234";

            CardTransactionResponse cardTransactionList
                    = cardTransactionService.getCardTransactionList(startDate, endDate, cardNumber);

            assertThat(cardTransactionList.cardTransactionList()).isEmpty();
        }

        @Test
        void 거래내역_3건() {
            OffsetDateTime startDate = OffsetDateTime.parse("2025-08-10T00:00:00+09:00");
            OffsetDateTime endDate = OffsetDateTime.parse("2025-08-15T00:00:00+09:00");
            String cardNumber = "123412341234";

            CardTransactionResponse cardTransactionList
                    = cardTransactionService.getCardTransactionList(startDate, endDate, cardNumber);

            List<CardTransactionResponse.CardTransactionInfo> result = cardTransactionList.cardTransactionList();

            assertThat(result).hasSize(3);

            assertThat(result)
                    .extracting(
                            CardTransactionResponse.CardTransactionInfo::merchantId,
                            CardTransactionResponse.CardTransactionInfo::amount,
                            CardTransactionResponse.CardTransactionInfo::transactionAt
                    )
                    .containsExactlyInAnyOrder(
                            Tuple.tuple(
                                    ct1.getMerchantId(),
                                    ct1.getAmount(),
                                    ct1.getTransactionAt().atOffset(ZoneOffset.UTC)
                            ),
                            Tuple.tuple(
                                    ct2.getMerchantId(),
                                    ct2.getAmount(),
                                    ct2.getTransactionAt().atOffset(ZoneOffset.UTC)
                            ),
                            Tuple.tuple(
                                    ct3.getMerchantId(),
                                    ct3.getAmount(),
                                    ct3.getTransactionAt().atOffset(ZoneOffset.UTC)
                            )
                    );
        }
    }
}