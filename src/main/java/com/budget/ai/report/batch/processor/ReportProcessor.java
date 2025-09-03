package com.budget.ai.report.batch.processor;

import com.budget.ai.report.dto.CategoryComparisonResult;
import com.budget.ai.report.dto.UserReportInput;
import com.budget.ai.transaction.dto.TransactionReportDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;

@Component
public class ReportProcessor implements ItemProcessor<UserReportInput, CategoryComparisonResult> {

    @Override
    public CategoryComparisonResult process(UserReportInput item) throws Exception {
        List<TransactionReportDTO> transactions = item.transactions();

        Map<String, BigDecimal> prevMonth = new HashMap<>();
        Map<String, BigDecimal> prevPrevMonth = new HashMap<>();

        for (TransactionReportDTO tr : transactions) {
            if (tr.month().equals(getPrevPrevMonth())) {
                prevPrevMonth.put(tr.categoryName(), tr.totalAmount());
            } else if (tr.month().equals(getPrevMonth())) {
                prevMonth.put(tr.categoryName(), tr.totalAmount());
            }
        }

        Map<String, BigDecimal> increaseCategory = new HashMap<>();
        Map<String, BigDecimal> decreaseCategory = new HashMap<>();

        Set<String> allCategories = new HashSet<>();
        allCategories.addAll(prevMonth.keySet());
        allCategories.addAll(prevPrevMonth.keySet());

        for (String category : allCategories) {
            BigDecimal prev = prevMonth.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal prevPrev = prevPrevMonth.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal diff = prev.subtract(prevPrev);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                increaseCategory.put(category, diff);
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                decreaseCategory.put(category, diff.abs());
            }
        }

        String reportMessage = buildReportMessage(increaseCategory, decreaseCategory);
        String notificationMessage = buildNotificationMessage(getPrevMonth());

        LocalDate yearMonth = YearMonth.now(ZoneOffset.UTC).minusMonths(1).atDay(1);

        return new CategoryComparisonResult(item.userId(), yearMonth, reportMessage, notificationMessage);
    }

    private String buildNotificationMessage(String prevMonth) {
        return prevMonth + " 리포트가 도착했습니다.";
    }

    private String buildReportMessage(Map<String, BigDecimal> inc, Map<String, BigDecimal> dec) {
        StringBuilder sb = new StringBuilder();

        if (!inc.isEmpty()) {
            sb.append("[증가된 카테고리]\n");
            inc.forEach((k, v) -> sb.append(String.format("- %s: +%,d원\n ", k, v.longValue())));
        }

        if (!dec.isEmpty()) {
            sb.append("[감소된 카테고리]\n");
            dec.forEach((k, v) -> sb.append(String.format("- %s: -%,d원 ", k, v.longValue())));
        }

        return sb.toString().trim();
    }

    private String getPrevMonth() {
        return YearMonth.now(ZoneOffset.UTC).minusMonths(1).toString(); // "YYYY-MM"
    }

    private String getPrevPrevMonth() {
        return YearMonth.now(ZoneOffset.UTC).minusMonths(2).toString(); // "YYYY-MM"
    }
}
