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
import com.budget.ai.transaction.dto.SumCategoryTransaction;
import com.budget.ai.transaction.dto.request.TransactionQueryRequest;
import com.budget.ai.transaction.dto.request.TransactionSyncRequest;
import com.budget.ai.transaction.dto.response.CategorySavingResponse;
import com.budget.ai.transaction.dto.response.ExternalTransactionResponse;
import com.budget.ai.transaction.dto.response.SumCategoryTransactionResponse;
import com.budget.ai.transaction.dto.response.TransactionResponse;
import com.budget.ai.user.User;
import com.budget.ai.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
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
    private final RedisTemplate<String, Object> redisTemplate;

    private final WebClient webClient;

    private final OpenAIService openAIService;

    public TransactionService(UserRepository userRepository, CardRepository cardRepository,
                              MerchantCategoryRepository merchantCategoryRepository,
                              CategoryRepository categoryRepository, TransactionRepository transactionRepository,
                              TransactionQueryRepository transactionQueryRepository,
                              RedisTemplate<String, Object> redisTemplate,
                              @Qualifier("serviceWebClient") WebClient webClient,
                              OpenAIService openAIService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.merchantCategoryRepository = merchantCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionQueryRepository = transactionQueryRepository;
        this.redisTemplate = redisTemplate;
        this.webClient = webClient;
        this.openAIService = openAIService;
    }

    /**
     * 카테고리별 거래 내역 통계
     * @param userId    로그인한 사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 카테고리별 거래 내역
     */
    @Cacheable(
            value = "sumCategoryTransaction",
            key = "#userId + ':' + #startDate.toString() + ':' + #endDate.toString()"
    )
    @Transactional(readOnly = true)
    public SumCategoryTransactionResponse getSumCategoryTransaction(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999

        List<SumCategoryTransaction.CategoryInfo> categoryInfoList = transactionQueryRepository.sumCategory(userId, startTime, endTime);

        BigDecimal totalSum = categoryInfoList.stream()
                .map(SumCategoryTransaction.CategoryInfo::sumAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SumCategoryTransactionResponse.SumCategoryInfo> responseList = categoryInfoList.stream()
                .map(info -> {
                    BigDecimal ratio = BigDecimal.ZERO;
                    if (totalSum.compareTo(BigDecimal.ZERO) > 0) {
                        ratio = info.sumAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalSum, 2, RoundingMode.HALF_UP);
                    }
                    return new SumCategoryTransactionResponse.SumCategoryInfo(
                            info.categoryId(),
                            info.categoryName(),
                            info.sumAmount(),
                            info.transactionCount(),
                            ratio
                    );
                })
                .toList();

        return new SumCategoryTransactionResponse(responseList, totalSum);
    }

    /**
     * 거래 내역 조회
     * @param request 거래내역 조회 필터링 및 조회 조건
     * @param userId  로그인한 사용자 ID
     * @return 조회 조건에 맞는 거래 내역
     */
    @Transactional(readOnly = true)
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
     * 거래내역 동기화
     * @param userId  로그인한 사용자 ID
     * @param request 동기화 시작 날짜, 종료 날짜
     */
    @Transactional
    public void syncTransaction(Long userId, TransactionSyncRequest request) {
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
            OffsetDateTime startDate = request.startDate().atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endDate = request.endDate().plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

            // 2-2. 카드 거래내역 API 호출
            ExternalTransactionResponse response = callCardTransactionAPI(card.getCardNumber(), startDate, endDate);

            // 3. 거래별 카테고리 매핑 및 Transaction 생성
            List<Transaction> transactionList = new ArrayList<>();

            for (ExternalTransactionResponse.TransactionInfo info : response.cardTransactionList()) {
                // 3-1. 이미 저장된 데이터는 pass
                boolean exists = transactionQueryRepository.existsTransaction(card.getId(), info.merchantId(), info.transactionAt().toLocalDateTime());

                if (exists) {
                    continue;
                }

                Optional<MerchantCategory> mcOpt = merchantCategoryRepository.findByMerchantNameLike(info.merchantName());
                // 3-2. 가맹점에 대한 카테고리가 이미 존재하는 경우 카테고리 지정
                Category category = mcOpt.map(MerchantCategory::getCategory)
                        .orElseGet(() -> {
                            // 3-3. 가맹점에 대한 카테고리가 존재하지 않는 경우 OpenAI API 호출하여 카테고리 지정
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
                        .transactionStatus(TransactionStatus.valueOf(info.cardTransactionStatus()))
                        .build();

                transactionList.add(transaction);
            }

            // 5. 카드별 거래내역 저장
            transactionRepository.saveAll(transactionList);
        }

        // 6. 카테고리별 카드 내역 통계 캐싱 무효화
        Set<String> keys = redisTemplate.keys("sumCategoryTransaction:" + userId + ":*");

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private ExternalTransactionResponse callCardTransactionAPI(String cardNumber, OffsetDateTime startDate, OffsetDateTime endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/outer/transaction")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
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
