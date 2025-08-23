package com.budget.ai.external.openai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * OpenAI API 데이터 요청 DTO
 */
@Schema(description = "OpenAI API 데이터 요청 DTO")
public record OpenAIRequest(

        // 모델
        String model,

        // 메세지
        List<Message> messages,

        // 텍스트 다양성
        Double temperature
) {
    public record Message(
            // system, user, assistant
            String role,

            // 내용
            String content
    ) {
    }
}
