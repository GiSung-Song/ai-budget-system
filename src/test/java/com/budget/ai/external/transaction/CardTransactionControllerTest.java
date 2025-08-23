package com.budget.ai.external.transaction;

import com.budget.ai.card.Card;
import com.budget.ai.card.CardCompanyType;
import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.external.transaction.dto.response.CardTransactionResponse;
import com.budget.ai.testsupport.ControllerTest;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest
class CardTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

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
        void 카드_거래내역_추가_성공() throws Exception {
            AddCardTransactionRequest request = new AddCardTransactionRequest(
                    "test-merchant-1",
                    null,
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234",
                    new BigDecimal("17500.00"),
                    "흥부 부대찌개 도봉점",
                    null,
                    OffsetDateTime.parse("2025-07-25T04:18:12+09:00"),
                    CardTransactionType.PAYMENT.name(),
                    CardTransactionStatus.APPROVED.name()
            );

            mockMvc.perform(post("/outer/transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());

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
        void 카드_거래내역_필수값_누락_400반환() throws Exception {
            AddCardTransactionRequest request = new AddCardTransactionRequest(
                    null,
                    null,
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234",
                    new BigDecimal("17500.00"),
                    "흥부 부대찌개 도봉점",
                    null,
                    OffsetDateTime.parse("2025-07-25T04:18:12+09:00"),
                    CardTransactionType.PAYMENT.name(),
                    CardTransactionStatus.APPROVED.name()
            );

            mockMvc.perform(post("/outer/transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 존재하지_않는_카드_404반환() throws Exception {
            AddCardTransactionRequest request = new AddCardTransactionRequest(
                    "test-merchant-1",
                    null,
                    CardCompanyType.HYUNDAI.name(),
                    "1313575724246868",
                    new BigDecimal("17500.00"),
                    "흥부 부대찌개 도봉점",
                    null,
                    OffsetDateTime.parse("2025-07-25T04:18:12+09:00"),
                    CardTransactionType.PAYMENT.name(),
                    CardTransactionStatus.APPROVED.name()
            );

            mockMvc.perform(post("/outer/transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        void 이미_등록된_카드_거래내역_고유ID_409반환() throws Exception {
            CardTransaction cardTransaction = testDataFactory.createCardTransaction(
                    "test-merchant-1",
                    "123412341234",
                    "50000.00",
                    "맥도날드 방학점",
                    "2025-08-15T03:22:32+09:00",
                    CardTransactionType.PAYMENT,
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
                    CardTransactionType.PAYMENT.name(),
                    CardTransactionStatus.APPROVED.name()
            );

            mockMvc.perform(post("/outer/transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class 카드_거래내역_조회_테스트 {

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
                    CardTransactionType.PAYMENT,
                    CardTransactionStatus.APPROVED
            );

            ct2 = testDataFactory.createCardTransaction(
                    "test-merchant-2",
                    "123412341234",
                    "29000.00",
                    "롯데리아 방학점",
                    "2025-08-12T21:20:58+09:00",
                    CardTransactionType.PAYMENT,
                    CardTransactionStatus.APPROVED
            );

            ct3 = testDataFactory.createCardTransaction(
                    "test-merchant-3",
                    "123412341234",
                    "38050.00",
                    "맥도날드 방학점",
                    "2025-08-14T15:38:04+09:00",
                    CardTransactionType.PAYMENT,
                    CardTransactionStatus.APPROVED
            );
        }

        @Test
        void 카드_거래내역_없음() throws Exception {
            mockMvc.perform(get("/outer/transaction")
                            .param("startDate", "2025-08-16T05:10:15+09:00")
                            .param("cardNumber", "123412341234"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardTransactionList").isEmpty());
        }

        @Test
        void 카드_거래내역_목록_조회() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/outer/transaction")
                            .param("startDate", "2025-08-01T05:10:15+09:00")
                            .param("cardNumber", "123412341234"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            CardTransactionResponse response = objectMapper.readValue(jsonResponse, CardTransactionResponse.class);

            assertThat(response.cardTransactionList())
                    .hasSize(3)
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