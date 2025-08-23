package com.budget.ai.transaction;

import com.budget.ai.card.Card;
import com.budget.ai.card.CardRepository;
import com.budget.ai.category.Category;
import com.budget.ai.category.CategoryRepository;
import com.budget.ai.category.MerchantCategory;
import com.budget.ai.category.MerchantCategoryRepository;
import com.budget.ai.external.openai.OpenAIService;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.transaction.dto.request.TransactionQueryRequest;
import com.budget.ai.transaction.dto.response.ExternalTransactionResponse;
import com.budget.ai.transaction.dto.response.TransactionResponse;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 거래내역 관련 Service
 */
@Slf4j
@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionQueryRepository transactionQueryRepository;

    private final WebClient webClient;

    private final OpenAIService openAIService;

    public TransactionService(UserRepository userRepository, CardRepository cardRepository,
                              MerchantCategoryRepository merchantCategoryRepository,
                              CategoryRepository categoryRepository, TransactionRepository transactionRepository,
                              TransactionQueryRepository transactionQueryRepository,
                              @Qualifier("serviceWebClient") WebClient webClient,
                              OpenAIService openAIService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.merchantCategoryRepository = merchantCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionQueryRepository = transactionQueryRepository;
        this.webClient = webClient;
        this.openAIService = openAIService;
    }

    /**
     * 거래 내역 조회
     * @param request 거래내역 조회 필터링 및 조회 조건
     * @param userId  로그인한 사용자 ID
     * @return 조회 조건에 맞는 거래 내역
     */
    public TransactionResponse getTransaction(TransactionQueryRequest request, Long userId) {
        List<Transaction> transactionList = transactionQueryRepository.search(request, userId);
        long totalElements = transactionQueryRepository.searchTotalElements(request, userId);
        int totalPages = (int) Math.ceil((double) totalElements / request.size());

        return new TransactionResponse(
                transactionList.stream()
                        .map(TransactionResponse.TransactionInfo::from)
                        .collect(Collectors.toList()),
                totalElements,
                totalPages,
                request.page(),
                request.size()
        );
    }

    /**
     * 최신 거래 내역 동기화
     * @param userId 로그인한 사용자 ID
     * @throws CustomException 회원이 없는 경우, 외부 API 호출 시 오류
     */
    public void lastSyncTransaction(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 보유한 카드 목록 조회
        List<Card> cardList = cardRepository.findAllByUserId(userId);

        // 1-1. 카드 목록이 없으면 return
        if (cardList.isEmpty()) {
            return;
        }

        for (Card card : cardList) {
            // 2. 카드 거래내역 조회
            // 2-1. 마지막 조회 시간이 없다면 해당 달의 첫날 초기화
            LocalDateTime utcTime =
                    card.getSynchronizedAt() != null
                            ? card.getSynchronizedAt()
                            : LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).atStartOfDay();
            OffsetDateTime startDate = utcTime.atOffset(ZoneOffset.UTC);

            // 2-2. 카드 거래내역 API 호출
            ExternalTransactionResponse response = callCardTransactionAPI(card.getCardNumber(), startDate);

            // 3. 거래별 카테고리 매핑 및 Transaction 생성
            List<Transaction> transactionList = new ArrayList<>();

            for (ExternalTransactionResponse.TransactionInfo info : response.cardTransactionList()) {
                Optional<MerchantCategory> mcOpt = merchantCategoryRepository.findByMerchantNameLike(info.merchantName());
                // 3-1. 가맹점에 대한 카테고리가 이미 존재하는 경우 카테고리 지정
                Category category = mcOpt.map(MerchantCategory::getCategory)
                        .orElseGet(() -> {
                            // 3-2. 가맹점에 대한 카테고리가 존재하지 않는 경우 OpenAI API 호출하여 카테고리 지정
                            String code = openAIService.chooseCategory(info.merchantName());

                            return categoryRepository.findByCode(code)
                                    .orElseThrow(() -> new CustomException(ErrorCode.API_CALL_WRONG_ANSWER));
                        });

                // 4. 거래내역 생성
                Transaction transaction = Transaction.builder()
                        .user(user)
                        .card(card)
                        .category(category)
                        .merchantId(info.merchantId())
                        .originalMerchantId(info.originalMerchantId())
                        .amount(info.amount())
                        .merchantName(info.merchantName())
                        .merchantAddress(info.merchantAddress())
                        .transactionAt(info.transactionAt()
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toLocalDateTime())
                        .transactionType(TransactionType.valueOf(info.cardTransactionType()))
                        .transactionStatus(TransactionStatus.valueOf(info.cardTransactionStatus()))
                        .build();

                transactionList.add(transaction);
            }

            // 5. 카드별 거래내역 저장
            transactionRepository.saveAll(transactionList);
        }
    }

    private ExternalTransactionResponse callCardTransactionAPI(String cardNumber, OffsetDateTime startDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/outer/transaction")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("cardNumber", cardNumber)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        Mono.error(new CustomException(ErrorCode.API_CALL_CLIENT_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new CustomException(ErrorCode.API_CALL_SERVER_ERROR)))
                .bodyToMono(ExternalTransactionResponse.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorMap(TimeoutException.class, ex -> new CustomException(ErrorCode.API_CALL_TIMEOUT))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof IOException || throwable instanceof TimeoutException))
                .block();
    }
}
