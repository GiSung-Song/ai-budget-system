package com.budget.ai.report.batch.listener;

import com.budget.ai.report.batch.dto.CategoryComparisonResult;
import com.budget.ai.report.batch.dto.UserReportInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.SkipListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterSkipListener implements SkipListener<UserReportInput, CategoryComparisonResult> {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DeadLetterSkipListener(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        saveDeadLetter("saveReportStep", null, t);
    }

    @Override
    public void onSkipInWrite(CategoryComparisonResult item, Throwable t) {
        saveDeadLetter("saveReportStep", item, t);
    }

    @Override
    public void onSkipInProcess(UserReportInput item, Throwable t) {
        saveDeadLetter("saveReportStep", item, t);
    }

    private void saveDeadLetter(String stepName, Object item, Throwable throwable) {
        String json = null;

        if (item != null) {
            try {
                json = objectMapper.writeValueAsString(item);
            } catch (JsonProcessingException e) {
                json = "{\"error\":\"Failed to serialize item\"}";
            }
        }

        jdbcTemplate.update(
                "INSERT INTO BATCH_DEAD_LETTER(step_name, input_data, exception_class, exception_message) VALUES (?, ?, ?, ?)",
                stepName,
                json,
                throwable.getClass().getName(),
                throwable.getMessage()
        );
    }
}
