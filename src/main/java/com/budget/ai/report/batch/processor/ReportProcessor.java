package com.budget.ai.report.batch.processor;

import com.budget.ai.report.batch.dto.CategoryComparisonResult;
import com.budget.ai.report.batch.dto.UserReportInput;
import com.budget.ai.transaction.dto.TransactionReportDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ReportProcessor implements ItemProcessor<UserReportInput, CategoryComparisonResult> {

    private static final DateTimeFormatter KOR_YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy년 MM월");
    private static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public CategoryComparisonResult process(UserReportInput item) throws Exception {
        List<TransactionReportDTO> transactions = item.transactions();

        YearMonth prevMonth = getPrevMonth();
        YearMonth prevPrevMonth = getPrevPrevMonth();

        Map<String, BigDecimal> prevMonthTr = new HashMap<>();
        Map<String, BigDecimal> prevPrevMonthTr = new HashMap<>();

        for (TransactionReportDTO tr : transactions) {
            YearMonth trYm = YearMonth.parse(tr.month(), YM_FORMATTER);

            if (trYm.equals(prevMonth)) {
                prevPrevMonthTr.put(tr.categoryName(), tr.totalAmount());
            } else if (trYm.equals(prevPrevMonth)) {
                prevMonthTr.put(tr.categoryName(), tr.totalAmount());
            }
        }

        Map<String, BigDecimal> increaseCategory = new HashMap<>();
        Map<String, BigDecimal> decreaseCategory = new HashMap<>();

        Set<String> allCategories = new HashSet<>();
        allCategories.addAll(prevMonthTr.keySet());
        allCategories.addAll(prevPrevMonthTr.keySet());

        for (String category : allCategories) {
            BigDecimal prev = prevMonthTr.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal prevPrev = prevPrevMonthTr.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal diff = prev.subtract(prevPrev);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                decreaseCategory.put(category, diff);
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                increaseCategory.put(category, diff.abs());
            }
        }

        String reportMessage = String.format(
                "%s vs %s 리포트\n\n%s",
                prevPrevMonth.format(KOR_YEAR_MONTH),
                prevMonth.format(KOR_YEAR_MONTH),
                buildReportMessage(increaseCategory, decreaseCategory)
        );

        String notificationMessage = buildNotificationMessage(prevMonth);

        LocalDate yearMonth = YearMonth.now(ZoneOffset.UTC).minusMonths(1).atDay(1);

        return new CategoryComparisonResult(item.userId(), yearMonth, reportMessage, notificationMessage);
    }

    private String buildNotificationMessage(YearMonth prevMonth) {
        return prevMonth.format(KOR_YEAR_MONTH) + " 리포트가 도착했습니다.";
    }

    private String buildReportMessage(Map<String, BigDecimal> inc, Map<String, BigDecimal> dec) {
        StringBuilder sb = new StringBuilder();

        sb.append("[증가된 카테고리]\n");
        if (!inc.isEmpty()) {
            inc.forEach((k, v) -> sb.append(String.format("- %s: +%,d원\n ", k, v.longValue())));
        }

        sb.append("[감소된 카테고리]\n");
        if (!dec.isEmpty()) {

            dec.forEach((k, v) -> sb.append(String.format("- %s: -%,d원 ", k, v.longValue())));
        }

        return sb.toString().trim();
    }

    private YearMonth getPrevMonth() {
        return YearMonth.now(ZoneOffset.UTC).minusMonths(1);
    }

    private YearMonth getPrevPrevMonth() {
        return YearMonth.now(ZoneOffset.UTC).minusMonths(2);
    }
}
