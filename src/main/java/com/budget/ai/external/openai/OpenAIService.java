package com.budget.ai.external.openai;

import com.budget.ai.external.openai.dto.OpenAIRequest;
import com.budget.ai.external.openai.dto.OpenAIResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
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

    @Value("${openai.api.key}")
    private String apiKey;
    private static final String URL = "/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    public String chooseCategory(String merchantName) {
        log.info(">>>>> OpenAI API 호출 시작 <<<<<<<<<<<<");

        String prompt = String.format(
                "다음 가맹점 이름을 보고 CAFE, FOOD, TRANSPORTATION, MART, CONVENIENCE_STORE, LIVING, CULTURE 중 하나로만 카테고리를 답해주세요." +
                        "만약 아무것도 속하지 않는다면 ETC 라고 답해주세요." +
                        "가맹점 이름: %s", merchantName
        );

        OpenAIRequest request = new OpenAIRequest(
                MODEL,
                List.of(new OpenAIRequest.Message("user", prompt)),
                0.0
        );

        Callable<String> call = RateLimiter.decorateCallable(rateLimiter, () -> {
            OpenAIResponse response = webClient.post()
                    .uri(URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIResponse.class)
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(2))
                                    .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
                    )
                    .block(Duration.ofSeconds(60));

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                log.error(">>>>> OpenAI API 호출 실패 <<<<<<<<");
                throw new CustomException(ErrorCode.API_CALL_WRONG_ANSWER);
            }

            String content = response.choices().get(0).message().content();
            if (content == null || !StringUtils.hasText(content)) {
                log.error(">>>>> OpenAI API 호출 실패 <<<<<<<<");
                throw new CustomException(ErrorCode.API_CALL_WRONG_ANSWER);
            }

            return content.trim().toUpperCase();
        });

        try {
            return call.call();
        } catch (RequestNotPermitted ex) {
            log.error(">>>>> OpenAI API RateLimiter 초과 <<<<<<<<");
            throw new CustomException(ErrorCode.API_RATE_LIMIT_EXCEEDED);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
