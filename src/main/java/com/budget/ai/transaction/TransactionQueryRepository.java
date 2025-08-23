package com.budget.ai.transaction;

import com.budget.ai.card.QCard;
import com.budget.ai.category.QCategory;
import com.budget.ai.transaction.dto.request.TransactionQueryRequest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.budget.ai.transaction.QTransaction.transaction;

@RequiredArgsConstructor
@Repository
public class TransactionQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public long searchTotalElements(TransactionQueryRequest request, Long userId) {
        return jpaQueryFactory
                .select(transaction.id.count())
                .from(transaction)
                .where(
                        transaction.user.id.eq(userId),
                        cardEq(request.cardId()),
                        categoryEq(request.categoryId()),
                        statusEq(request.transactionStatus()),
                        typeEq(request.transactionType()),
                        merchantNameContains(request.merchantName()),
                        amountGoe(request.amountMin()),
                        amountLoe(request.amountMax()),
                        dateBetween(request.startDate(), request.endDate())
                )
                .fetchOne();
    }

    public List<Transaction> search(TransactionQueryRequest request, Long userId) {
        return jpaQueryFactory
                .selectFrom(transaction)
                .join(transaction.card, QCard.card).fetchJoin()
                .join(transaction.category, QCategory.category).fetchJoin()
                .where(
                        transaction.user.id.eq(userId),
                        cardEq(request.cardId()),
                        categoryEq(request.categoryId()),
                        statusEq(request.transactionStatus()),
                        typeEq(request.transactionType()),
                        merchantNameContains(request.merchantName()),
                        amountGoe(request.amountMin()),
                        amountLoe(request.amountMax()),
                        dateBetween(request.startDate(), request.endDate())
                )
                .offset(request.page() * request.size())
                .limit(request.size())
                .orderBy(orderBy(request.sortOrder()))
                .fetch();
    }

    private BooleanExpression cardEq(Long cardId) {
        return cardId != null ? transaction.card.id.eq(cardId) : null;
    }

    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId != null ? transaction.category.id.eq(categoryId) : null;
    }

    private BooleanExpression statusEq(TransactionStatus status) {
        return status != null ? transaction.transactionStatus.eq(status) : null;
    }

    private BooleanExpression typeEq(TransactionType type) {
        return type != null ? transaction.transactionType.eq(type) : null;
    }

    private BooleanExpression merchantNameContains(String merchantName) {
        return merchantName != null && !merchantName.isBlank() ? transaction.merchantName.contains(merchantName) : null;
    }

    private BooleanExpression amountGoe(BigDecimal amountMin) {
        return amountMin != null ? transaction.amount.goe(amountMin) : null;
    }

    private BooleanExpression amountLoe(BigDecimal amountMax) {
        return amountMax != null ? transaction.amount.loe(amountMax) : null;
    }

    private BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return null;
        return transaction.transactionAt.between(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }

    private OrderSpecifier<?> orderBy(TransactionQueryRequest.SortOrder sortOrder) {
        return sortOrder == TransactionQueryRequest.SortOrder.ASC
                ? transaction.transactionAt.asc()
                : transaction.transactionAt.desc();
    }
}
