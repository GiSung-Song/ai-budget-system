package com.budget.ai.external.openai;

import com.budget.ai.external.openai.dto.OpenAIRequest;
import com.budget.ai.external.openai.dto.OpenAIResponse;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class OpenAIService {

    private final WebClient webClient;

    public OpenAIService(@Qualifier("openAIWebClient") WebClient webClient) {
        this.webClient = webClient;
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

        OpenAIResponse response = webClient.post()
                .uri(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .block(Duration.ofSeconds(5));

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
    }
}
