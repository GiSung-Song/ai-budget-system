package com.budget.ai.card;

import com.budget.ai.testsupport.RepositoryTest;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUser() {
        testUser = userRepository.save(
                User.builder()
                        .name("테스터")
                        .email("tester@email.com")
                        .password("rawPassword")
                        .build()
        );

        testCard = cardRepository.save(
                Card.builder()
                        .cardCompanyType(CardCompanyType.HYUNDAI)
                        .cardNumber("123412341234")
                        .user(testUser)
                        .build()
        );
    }

    @Nested
    class 카드_등록_여부_체크_테스트 {

        @Test
        void 카드_등록_여부_true() {
            boolean result = cardRepository.existsByCardCompanyTypeAndCardNumber(CardCompanyType.HYUNDAI, testCard.getCardNumber());

            assertThat(result).isTrue();
        }

        @Test
        void 카드_등록_여부_false() {
            boolean result = cardRepository.existsByCardCompanyTypeAndCardNumber(CardCompanyType.NH, "1313131313");

            assertThat(result).isFalse();
        }
    }

    @Nested
    class 카드_조회_테스트 {

        @Test
        void 카드_존재() {
            Optional<Card> card = cardRepository.findByIdAndUserId(testCard.getId(), testUser.getId());

            assertThat(card).isPresent();
        }

        @Test
        void 카드_존재하지_않음() {
            Optional<Card> card = cardRepository.findByIdAndUserId(testCard.getId(), 543214321L);

            assertThat(card).isEmpty();
        }
    }

    @Nested
    class 카드_목록_조회_테스트 {

        @Test
        void 카드_목록_조회_단건() {
            List<Card> cardLIst = cardRepository.findAllByUserId(testUser.getId());

            assertThat(cardLIst).hasSize(1);
        }

        @Test
        void 카드_목록_조회_다건() {
            Card card1 = Card.builder()
                    .cardCompanyType(CardCompanyType.HYUNDAI)
                    .cardNumber("131313242424")
                    .user(testUser)
                    .build();

            Card card2 = Card.builder()
                    .cardCompanyType(CardCompanyType.SAMSUNG)
                    .cardNumber("242424131313")
                    .user(testUser)
                    .build();

            cardRepository.saveAll(List.of(card1, card2));

            List<Card> cardList = cardRepository.findAllByUserId(testUser.getId());

            assertThat(cardList)
                    .hasSize(3)
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactlyInAnyOrder(card1, card2, testCard);
        }

        @Test
        void 카드_목록_조회_없음() {
            User user = userRepository.save(
                    User.builder()
                            .name("테스터2")
                            .email("tester2@email.com")
                            .password("rawPassword")
                            .build()
            );

            List<Card> cardList = cardRepository.findAllByUserId(user.getId());

            assertThat(cardList).isEmpty();
        }
    }
}