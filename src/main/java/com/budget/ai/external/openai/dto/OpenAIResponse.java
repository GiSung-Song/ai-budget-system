package com.budget.ai.external.openai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * OpenAI API 데이터 응답 DTO
 */
@Schema(description = "OpenAI API 데이터 응답 DTO")
public record OpenAIResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {

    }
}
