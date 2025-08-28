package com.budget.ai.transaction;

import com.budget.ai.card.QCard;
import com.budget.ai.transaction.dto.request.TransactionQueryRequest;
import com.budget.ai.transaction.dto.SumCategoryTransaction;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.budget.ai.category.QCategory.*;
import static com.budget.ai.transaction.QTransaction.transaction;

@RequiredArgsConstructor
@Repository
public class TransactionQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<SumCategoryTransaction.CategoryInfo> sumCategory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {

        // 순 지출 금액 (승인 + 취소 상쇄)
        NumberExpression<BigDecimal> netAmount = new CaseBuilder()
                .when(transaction.transactionStatus.eq(TransactionStatus.APPROVED))
                .then(transaction.amount)
                .when(transaction.transactionStatus.eq(TransactionStatus.CANCELED)
                        .and(transaction.originalMerchantId.isNotNull()))
                .then(transaction.amount.negate())
                .otherwise(BigDecimal.ZERO);

        NumberExpression<BigDecimal> netAmountSum = netAmount.sum();

        // 카테고리별 합계 + 거래 수 조회
        return jpaQueryFactory
                .select(Projections.constructor(
                    SumCategoryTransaction.CategoryInfo.class,
                        transaction.category.id,
                        transaction.category.displayName,
                        netAmountSum,
                        transaction.id.count()
                ))
                .from(transaction)
                .join(transaction.category, category)
                .where(
                        transaction.user.id.eq(userId),
                        transaction.transactionAt.between(startDate, endDate)
                )
                .groupBy(transaction.category.id, transaction.category.displayName)
                .orderBy(netAmountSum.desc())
                .fetch();
    }

    public boolean existsTransaction(Long cardId, String merchantId, LocalDateTime transactionAt) {
        return jpaQueryFactory
                .selectOne()
                .from(transaction)
                .where(
                        transaction.card.id.eq(cardId),
                        transaction.merchantId.eq(merchantId),
                        transaction.transactionAt.eq(transactionAt)
                )
                .fetchFirst() != null;
    }

    public long searchTotalElements(TransactionQueryRequest request, Long userId) {
        return jpaQueryFactory
                .select(transaction.id.count())
                .from(transaction)
                .where(
                        transaction.user.id.eq(userId),
                        cardEq(request.cardId()),
                        categoryEq(request.categoryId()),
                        statusEq(request.transactionStatus()),
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
                .join(transaction.category, category).fetchJoin()
                .where(
                        transaction.user.id.eq(userId),
                        cardEq(request.cardId()),
                        categoryEq(request.categoryId()),
                        statusEq(request.transactionStatus()),
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
