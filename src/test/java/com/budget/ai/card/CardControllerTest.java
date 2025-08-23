package com.budget.ai.card;

import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.testsupport.ControllerTest;
import com.budget.ai.testsupport.TestAuthHelper;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest
class CardControllerTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private String accessToken;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
        TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

        accessToken = tokenResponse.accessToken();
    }

    @Nested
    class 카드_등록_테스트 {

        @Test
        void 카드_등록_성공() throws Exception {
            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.KB.name(),
                    "123412341234"
            );

            mockMvc.perform(post("/api/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated());

            Optional<Card> findCard = cardRepository.findByCardCompanyTypeAndCardNumberAndUserId(
                    CardCompanyType.KB, "123412341234", user.getId()
            );

            assertThat(findCard).isPresent();
            assertThat(findCard.get().getCardCompanyType()).isEqualTo(CardCompanyType.KB);
            assertThat(findCard.get().getCardNumber()).isEqualTo(request.cardNumber());
            assertThat(findCard.get().getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        void 유효하지_않은_요청_데이터_400반환() throws Exception {
            RegisterCardRequest request = new RegisterCardRequest(
                    null,
                    "123412341234"
            );

            mockMvc.perform(post("/api/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 로그인_필요_401반환() throws Exception {
            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.KB.name(),
                    "123412341234"
            );

            mockMvc.perform(post("/api/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_회원_404반환() throws Exception {
            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.KB.name(),
                    "123412341234"
            );

            userRepository.delete(user);

            mockMvc.perform(post("/api/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        void 이미_등록된_카드_409반환() throws Exception {
            Card card = Card.builder()
                    .cardCompanyType(CardCompanyType.KB)
                    .cardNumber("123412341234")
                    .user(user)
                    .build();

            cardRepository.save(card);

            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.KB.name(),
                    "123412341234"
            );

            mockMvc.perform(post("/api/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class 카드_삭제_테스트 {

        private Card card;

        @BeforeEach
        void setUpCard() {
            card = testDataFactory.createCard(CardCompanyType.KB, "123412341234", user);
        }

        @Test
        void 카드_삭제_성공() throws Exception {
            mockMvc.perform(delete("/api/cards/{cardId}", card.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk());

            Optional<Card> findCard = cardRepository.findByIdAndUserId(card.getId(), user.getId());

            assertThat(findCard).isEmpty();
        }

        @Test
        void 유효하지_않은_경로변수_400반환() throws Exception {
            mockMvc.perform(delete("/api/cards/{cardId}", "fdsafdsa")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 로그인_필요_401반환() throws Exception {
            mockMvc.perform(delete("/api/cards/{cardId}", card.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_카드_404반환() throws Exception {
            mockMvc.perform(delete("/api/cards/{cardId}", 43214321L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 카드_목록_조회_테스트 {

        @Test
        void 카드_목록_없음() throws Exception {
            mockMvc.perform(get("/api/cards")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cardList").isArray())
                    .andExpect(jsonPath("$.data.cardList.length()").value(0));
        }

        @Test
        void 카드_목록_다건() throws Exception {
            Card card1 = testDataFactory.createCard(CardCompanyType.KB, "123412341234", user);
            Card card2 = testDataFactory.createCard(CardCompanyType.SAMSUNG, "432143214321", user);

            mockMvc.perform(get("/api/cards")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cardList").isArray())
                    .andExpect(jsonPath("$.data.cardList.length()").value(2))
                    .andExpect(jsonPath("$.data.cardList[*].cardCompanyType",
                            containsInAnyOrder("국민카드", "삼성카드")))
                    .andExpect(jsonPath("$.data.cardList[*].cardNumber",
                            containsInAnyOrder("123412341234", "432143214321")));
        }

        @Test
        void 로그인_필요_401반환() throws Exception {
            mockMvc.perform(get("/api/card"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}