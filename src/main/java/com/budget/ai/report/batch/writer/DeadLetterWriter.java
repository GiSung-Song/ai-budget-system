package com.budget.ai.report.batch.writer;

import com.budget.ai.report.Report;
import com.budget.ai.report.ReportRepository;
import com.budget.ai.report.batch.dto.CategoryComparisonResult;
import com.budget.ai.report.batch.dto.DeadLetterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadLetterWriter implements ItemWriter<DeadLetterResult>, StepExecutionListener {

    private final ReportRepository reportRepository;
    private StepExecution stepExecution;

    @Override
    public void write(Chunk<? extends DeadLetterResult> chunk) throws Exception {
        List<? extends DeadLetterResult> items = chunk.getItems();

        List<CategoryComparisonResult> results = chunk.getItems().stream()
                .map(DeadLetterResult::result)
                .toList();

        List<Report> reports = results.stream()
                .map(dto -> Report.builder()
                        .userId(dto.userId())
                        .reportMonth(dto.yearMonth())
                        .reportMessage(dto.reportMessage())
                        .notificationMessage(dto.notificationMessage())
                        .build()
                )
                .toList();

        reportRepository.saveAll(reports);

        List<Long> successIds = stepExecution.getExecutionContext()
                .get("successDeadLetterIds", List.class);

        if (successIds == null) successIds = new ArrayList<>();

        for (DeadLetterResult item : items) {
            successIds.add(item.deadLetterId());
        }

        stepExecution.getExecutionContext().put("successDeadLetterIds", successIds);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}