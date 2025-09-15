package com.budget.ai.external.openai;

import com.budget.ai.external.openai.dto.OpenAIRequest;
import com.budget.ai.external.openai.dto.OpenAIResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.transaction.dto.response.SumCategoryTransactionResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final RateLimiter rateLimiter;

    public OpenAIService(@Qualifier("openAIWebClient") WebClient webClient) {
        this.webClient = webClient;

        //3초당 5회 요청 제한, 타임아웃 10초
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(3))
                .timeoutDuration(Duration.ofSeconds(10))
                .build();

        this.rateLimiter = RateLimiter.of("openai", config);
    }

    private static final String URL = "/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    /**
     * 카테고리 자동 매핑
     * @param merchantName 상호명
     * @return 카테고리명
     */
    public String chooseCategory(String merchantName) {
        String prompt = String.format(
                "다음 가맹점 이름을 보고 CAFE, FOOD, TRANSPORTATION, MART, CONVENIENCE_STORE, LIVING, CULTURE 중 하나로만 카테고리를 답해주세요." +
                        "만약 아무것도 속하지 않는다면 ETC 라고 답해주세요." +
                        "가맹점 이름: %s", merchantName
        );

        String content = callOpenAIApi(prompt);

        return content.trim().toUpperCase();
    }

    /**
     * 카테고리별 절약 방법 추천
     * @param response 카테고리별 정보
     * @return
     */
    public String recommendSavingForCategory(SumCategoryTransactionResponse response) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("총 금액 : ").append(response.totalSum()).append("원\n");

        for (SumCategoryTransactionResponse.SumCategoryInfo info : response.sumCategoryInfoList()) {
            promptBuilder.append(info.categoryName())
                    .append(" - ")
                    .append(info.sumAmount())
                    .append("원\n")
                    .append(" : ")
                    .append(info.ratio())
                    .append("%\n");
        }

        promptBuilder.append("위 통계를 바탕으로 각 카테고리별로 절약할 만한 방법을 추천해주세요. ")
                .append("답변은 JSON 형태로 반환해주세요. 예시는 {\"교통\":\"택시 \", \"편의점\":\"간식 구매 줄이기\"} 와 같이 반환해주세요.")
                .append("또한, ```json``` 같은 마크다운 블록은 제거하고 순수 JSON만 반환해주세요.");

        return callOpenAIApi(promptBuilder.toString());
    }

    private String callOpenAIApi(String prompt) {
        OpenAIRequest request = new OpenAIRequest(
                MODEL,
                List.of(new OpenAIRequest.Message("user", prompt)),
                0.0
        );

        Callable<String> call = RateLimiter.decorateCallable(rateLimiter, () -> {
            OpenAIResponse response = webClient.post()
                    .uri(URL)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIResponse.class)
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(2))
                                    .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
                    )
                    .block(Duration.ofSeconds(60));

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new CustomException(ErrorCode.API_CALL_WRONG_ANSWER);
            }

            String content = response.choices().get(0).message().content();
            if (content == null || !StringUtils.hasText(content)) {
                throw new CustomException(ErrorCode.API_CALL_WRONG_ANSWER);
            }

            System.out.println("content = " + content);

            return content;
        });

        try {
            return call.call();
        } catch (RequestNotPermitted ex) {
            throw new CustomException(ErrorCode.API_RATE_LIMIT_EXCEEDED);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
