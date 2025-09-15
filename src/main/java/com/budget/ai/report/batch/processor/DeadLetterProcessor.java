package com.budget.ai.report.batch.processor;

import com.budget.ai.report.batch.dto.CategoryComparisonResult;
import com.budget.ai.report.batch.dto.DeadLetterItem;
import com.budget.ai.report.batch.dto.DeadLetterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeadLetterProcessor implements ItemProcessor<DeadLetterItem, DeadLetterResult> {

    private final ReportProcessor reportProcessor;

    @Override
    public DeadLetterResult process(DeadLetterItem item) throws Exception {
        CategoryComparisonResult result = reportProcessor.process(item.input());

        return new DeadLetterResult(result, item.id());
    }
}
