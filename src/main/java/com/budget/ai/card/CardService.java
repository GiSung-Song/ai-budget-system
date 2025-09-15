package com.budget.ai.card;

import com.budget.ai.card.dto.request.RegisterCardRequest;
import com.budget.ai.card.dto.response.CardInfoResponse;
import com.budget.ai.logging.AuditLogUtil;
import com.budget.ai.logging.aop.AuditLEntityId;
import com.budget.ai.logging.aop.AuditLog;
import com.budget.ai.logging.aop.OperationLog;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 카드 관련 Service
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * 카드 등록
     * @param userId  로그인한 사용자 ID
     * @param request 카드 등록 요청 DTO
     * @throws CustomException 회원이 없거나, 이미 등록된 카드인 경우
     */
    @Transactional
    @OperationLog(eventName = "카드 등록")
    public void registerCard(Long userId, RegisterCardRequest request) {
        Card savedCard = null;
        boolean success = false;
        String message = null;

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            boolean existsCard = cardRepository.existsByCardCompanyTypeAndCardNumber(
                    CardCompanyType.valueOf(request.cardCompanyType()), request.cardNumber());

            if (existsCard) {
                throw new CustomException(ErrorCode.CARD_ALREADY_EXISTS);
            }

            Card cardCompany = Card.builder()
                    .cardCompanyType(CardCompanyType.valueOf(request.cardCompanyType()))
                    .cardNumber(request.cardNumber())
                    .user(user)
                    .build();

            savedCard = cardRepository.save(cardCompany);

            success = true;
            message = "카드 등록 완료";
        } catch (CustomException exception) {
            message = "ErrorCode: " + exception.getErrorCode().getCode() + ", Message: " + exception.getErrorCode().getMessage();
            throw exception;
        } finally {
            AuditLogUtil.logAudit(
                    this,
                    new Object[]{request},
                    "카드 등록",
                    "registerCard",
                    "INSERT",
                    "cards",
                    savedCard != null ? String.valueOf(savedCard.getId()) : null,
                    message,
                    success
            );
        }

    }

    /**
     * 카드 삭제
     * @param userId 로그인한 사용자 ID
     * @param cardId 카드 ID
     * @throws CustomException 해당 카드가 없는 경우
     */
    @Transactional
    @OperationLog(eventName = "카드 삭제")
    @AuditLog(eventName = "카드 삭제", operationType = "DELETE", entity = "cards")
    public void deleteCard(Long userId, @AuditLEntityId Long cardId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CARD_NOT_FOUND));

        cardRepository.delete(card);
    }

    /**
     * 카드 목록 조회
     * @param userId 로그인한 사용자 ID
     * @return 카드 목록
     */
    @Transactional(readOnly = true)
    @OperationLog(eventName = "카드 목록 조회")
    public CardInfoResponse getMyCardInfo(Long userId) {
        List<Card> cardList = Optional.ofNullable(
                cardRepository.findAllByUserId(userId)
        ).orElse(Collections.emptyList());

        List<CardInfoResponse.CardInfo> cardInfoList = cardList.stream()
                .map(card -> CardInfoResponse.CardInfo.from(card))
                .collect(Collectors.toList());

        return new CardInfoResponse(cardInfoList);
    }
}