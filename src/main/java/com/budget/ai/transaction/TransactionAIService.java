package com.budget.ai.transaction;

import com.budget.ai.external.openai.OpenAIService;
import com.budget.ai.logging.aop.OperationLog;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.transaction.dto.response.CategorySavingResponse;
import com.budget.ai.transaction.dto.response.SumCategoryTransactionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransactionAIService {

    private final TransactionService transactionService;
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;

    /**
     * 카테고리별 절약 추천 방법
     * @param userId    로그인한 사용자 ID
     * @param startDate 조회 시작 날짜
     * @param endDate   조회 종료 날짜
     * @return
     */
    @OperationLog(eventName = "카테고리별 절약 방법 추천")
    public CategorySavingResponse recommendSaving(Long userId, LocalDate startDate, LocalDate endDate) {
        // 1. 카테고리별 거래 내역 통계 조회
        SumCategoryTransactionResponse sumCategoryTransaction = transactionService.getSumCategoryTransaction(userId, startDate, endDate);

        // 2. 통계를 가지고 OepnAI API 호출
        String jsonString = openAIService.recommendSavingForCategory(sumCategoryTransaction);

        try {
            Map<String, String> map = objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});

            List<CategorySavingResponse.SavingInfo> savingInfoList = map.entrySet().stream()
                    .map(e -> new CategorySavingResponse.SavingInfo(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            return new CategorySavingResponse(savingInfoList);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.CONVERT_JSON_PARSING);
        }
    }
}
