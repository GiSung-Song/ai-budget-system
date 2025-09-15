package com.budget.ai.report.batch.reader;

import com.budget.ai.report.batch.dto.DeadLetterItem;
import com.budget.ai.report.batch.dto.UserReportInput;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadLetterReader implements ItemReader<DeadLetterItem> {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private List<DeadLetterItem> items;
    private int index = 0;

    @Override
    public DeadLetterItem read() throws Exception {
        if (items == null) {
            items = jdbcTemplate.query(
                    "SELECT id, input_data FROM BATCH_DEAD_LETTER ORDER BY created_at ASC",
                    (rs, rowNum) -> {
                        String json = rs.getString("input_data");
                        UserReportInput input = null;
                        try {
                            input = objectMapper.readValue(json, UserReportInput.class);
                        } catch (JsonProcessingException e) {
                            throw new CustomException(ErrorCode.BATCH_RUN_ERROR);
                        }
                        return new DeadLetterItem(rs.getLong("id"), input);
                    }
            );
        }

        if (index < items.size()) {
            return items.get(index++);
        }

        return null;
    }
}
