package com.budget.ai.testsupport;

import com.budget.ai.card.Card;
import com.budget.ai.card.CardCompanyType;
import com.budget.ai.card.CardRepository;
import com.budget.ai.external.transaction.CardTransaction;
import com.budget.ai.external.transaction.CardTransactionRepository;
import com.budget.ai.external.transaction.CardTransactionStatus;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Profile("test")
@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(String name, String email, String password) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        return userRepository.save(user);
    }

    public Card createCard(CardCompanyType cardCompanyType, String cardNumber, User user) {
        Card card = Card.builder()
                .cardCompanyType(cardCompanyType)
                .cardNumber(cardNumber)
                .user(user)
                .build();

        return cardRepository.save(card);
    }

    public CardTransaction createCardTransaction(String merchantId, String cardNumber, String amount, String merchantName,
                                                 String transactionAt, CardTransactionStatus status) {
        CardTransaction cardTransaction = CardTransaction.builder()
                .merchantId(merchantId)
                .cardNumber(cardNumber)
                .amount(new BigDecimal(amount))
                .merchantName(merchantName)
                .transactionAt(OffsetDateTime.parse(transactionAt)
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime())
                .cardTransactionStatus(status)
                .build();

        return cardTransactionRepository.save(cardTransaction);
    }
}