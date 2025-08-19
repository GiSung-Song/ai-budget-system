package com.budget.ai.external.transaction;

import com.budget.ai.card.CardCompanyType;
import com.budget.ai.card.CardRepository;
import com.budget.ai.external.transaction.dto.request.AddCardTransactionRequest;
import com.budget.ai.external.transaction.dto.response.CardTransactionResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardTransactionService {

    private final CardTransactionRepository cardTransactionRepository;
    private final CardRepository cardRepository;

    /**
     * 카드 거래 내역 추가
     * @param request 카드 거래 내역 추가 요청 DTO
     * @throws CustomException 카드사와 카드번호로 등록된 카드인지 조회 후 존재하지 않으면 발생
     */
    public void addCardTransaction(AddCardTransactionRequest request) {
        boolean existsTransaction = cardTransactionRepository.existsByMerchantIdAndCardNumber(request.merchantId(), request.cardNumber());

        if (existsTransaction) {
            throw new CustomException(ErrorCode.CARD_TRANSACTION_ALREADY_EXISTS);
        }

        boolean existsCard = cardRepository.existsByCardCompanyTypeAndCardNumber(
                CardCompanyType.valueOf(request.cardCompanyType()), request.cardNumber());

        if (!existsCard) {
            throw new CustomException(ErrorCode.CARD_NOT_FOUND);
        }

        CardTransaction cardTransaction = CardTransaction.builder()
                .merchantId(request.merchantId())
                .originalMerchantId(request.originalMerchantId())
                .cardNumber(request.cardNumber())
                .amount(request.amount())
                .merchantName(request.merchantName())
                .merchantAddress(request.merchantAddress())
                .transactionAt(request.transactionAt()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime())
                .cardTransactionType(CardTransactionType.valueOf(request.cardTransactionType()))
                .cardTransactionStatus(CardTransactionStatus.valueOf(request.cardTransactionStatus()))
                .build();

        cardTransactionRepository.save(cardTransaction);
    }

    /**
     * 특정 날짜 이후의 카드 거래 내역 조회
     * @param startDate  조회 시작 날짜
     * @param cardNumber 카드 번호
     * @return 카드 거래 내역
     */
    public CardTransactionResponse getCardTransactionList(OffsetDateTime startDate, String cardNumber) {
        LocalDateTime converted = startDate
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        List<CardTransaction> cardTransactionList = cardTransactionRepository.findByCardNumberAndTransactionAtAfter(
                cardNumber, converted
        );

        List<CardTransactionResponse.CardTransactionInfo> cardTransactionInfoList = cardTransactionList.stream()
                .map(cardTransaction -> CardTransactionResponse.CardTransactionInfo.from(cardTransaction))
                .collect(Collectors.toList());

        return new CardTransactionResponse(cardTransactionInfoList);
    }
}