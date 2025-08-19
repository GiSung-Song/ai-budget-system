package com.budget.ai.card;

import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.card.dto.response.CardInfoResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.testsupport.ServiceTest;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ServiceTest
class CardServiceTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private TestDataFactory testDataFactory;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
    }

    @Nested
    class 카드_등록_테스트 {

        @Test
        void 카드_등록_성공() {
            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234"
            );

            cardService.registerCard(testUser.getId(), request);

            Optional<Card> findCard = cardRepository.findByCardCompanyTypeAndCardNumberAndUserId(
                    CardCompanyType.valueOf(request.cardCompanyType()), request.cardNumber(), testUser.getId());

            assertThat(findCard).isPresent();
            assertThat(findCard.get().getCardCompanyType()).isEqualTo(CardCompanyType.HYUNDAI);
            assertThat(findCard.get().getCardNumber()).isEqualTo(request.cardNumber());
            assertThat(findCard.get().getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        void 회원_없음_404반환() {
            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234"
            );

            assertThatThrownBy(() -> cardService.registerCard(123421L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void 중복된_카드_409반환() {
            RegisterCardRequest request = new RegisterCardRequest(
                    CardCompanyType.HYUNDAI.name(),
                    "123412341234"
            );

            Card card = testDataFactory.createCard(CardCompanyType.HYUNDAI, "123412341234", testUser);

            assertThatThrownBy(() -> cardService.registerCard(testUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CARD_ALREADY_EXISTS);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

    @Nested
    class 카드_삭제_테스트 {

        @Test
        void 카드_삭제_정상() {
            Card card = testDataFactory.createCard(CardCompanyType.HYUNDAI, "123412341234", testUser);
            cardService.deleteCard(testUser.getId(), card.getId());

            Optional<Card> findCard = cardRepository.findById(card.getId());

            assertThat(findCard).isEmpty();
        }

        @Test
        void 없는_카드일_경우_404반환() {
            assertThatThrownBy(() -> cardService.deleteCard(testUser.getId(), 432143214321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CARD_NOT_FOUND);
                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class 카드_목록_조회 {

        @Test
        void 카드_비어있는_경우() {
            CardInfoResponse myCardInfo = cardService.getMyCardInfo(testUser.getId());

            assertThat(myCardInfo.cardList()).isEmpty();
        }

        @Test
        void 카드_목록_조회_다건() {
            Card card1 = testDataFactory.createCard(CardCompanyType.HYUNDAI, "123412341234", testUser);
            Card card2 = testDataFactory.createCard(CardCompanyType.NH, "1313131313", testUser);
            Card card3 = testDataFactory.createCard(CardCompanyType.SHINHAN, "2424242424", testUser);

            CardInfoResponse myCardInfo = cardService.getMyCardInfo(testUser.getId());

            List<CardInfoResponse.CardInfo> cardInfoList = List.of(
                    CardInfoResponse.CardInfo.from(card1),
                    CardInfoResponse.CardInfo.from(card2),
                    CardInfoResponse.CardInfo.from(card3)
            );

            assertThat(myCardInfo.cardList())
                    .hasSize(3)
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactlyInAnyOrderElementsOf(cardInfoList);
        }
    }
}