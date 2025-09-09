package com.budget.ai.report.batch.reader;

import com.budget.ai.report.batch.dto.UserReportInput;
import com.budget.ai.transaction.TransactionQueryRepository;
import com.budget.ai.transaction.dto.TransactionReportDTO;
import com.budget.ai.user.UserQueryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
@StepScope
public class ReportReader implements ItemReader<UserReportInput> {

    private final TransactionQueryRepository transactionQueryRepository;
    private final UserQueryRepository userQueryRepository;

    @Value("#{jobParameters['startDate']}")
    private String startDateStr;

    @Value("#{jobParameters['endDate']}")
    private String endDateStr;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Iterator<Long> userIdIterator;

    @PostConstruct
    public void init() {
        // JobParameter String → LocalDateTime 변환
        startDate = LocalDateTime.parse(startDateStr);
        endDate = LocalDateTime.parse(endDateStr);
    }

    @Override
    public UserReportInput read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (userIdIterator == null) {
            List<Long> userIds = userQueryRepository.getAllUserId();
            userIdIterator = userIds.iterator();
        }

        if (!userIdIterator.hasNext()) return null;

        Long userId = userIdIterator.next();

        List<TransactionReportDTO> transactionCategorySum = transactionQueryRepository.getTransactionCategorySum(userId, startDate, endDate);

        return new UserReportInput(userId, transactionCategorySum);
    }
}