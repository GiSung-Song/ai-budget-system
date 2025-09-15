package com.budget.ai.report.batch.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadLetterStepListener implements StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        List<Long> successIds = stepExecution.getExecutionContext()
                .get("successDeadLetterIds", List.class);

        if (successIds != null && !successIds.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "DELETE FROM BATCH_DEAD_LETTER WHERE id = ?",
                    successIds,
                    successIds.size(),
                    (ps, id) -> ps.setLong(1, id)
            );
        }

        return stepExecution.getExitStatus();
    }
}