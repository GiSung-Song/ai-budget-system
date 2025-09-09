package com.budget.ai.report.batch.writer;

import com.budget.ai.report.Report;
import com.budget.ai.report.ReportRepository;
import com.budget.ai.report.batch.dto.CategoryComparisonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportWriter implements ItemWriter<CategoryComparisonResult> {

    private final ReportRepository reportRepository;

    @Override
    public void write(Chunk<? extends CategoryComparisonResult> chunk) throws Exception {
        List<? extends CategoryComparisonResult> items = chunk.getItems();

        List<Report> reports = items.stream()
                .map(dto -> Report.builder()
                        .userId(dto.userId())
                        .reportMonth(dto.yearMonth())
                        .reportMessage(dto.reportMessage())
                        .notificationMessage(dto.notificationMessage())
                        .build()
                )
                .toList();

        reportRepository.saveAll(reports);
    }
}
