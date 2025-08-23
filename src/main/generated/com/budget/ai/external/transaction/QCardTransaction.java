package com.budget.ai.external.transaction;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCardTransaction is a Querydsl query type for CardTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCardTransaction extends EntityPathBase<CardTransaction> {

    private static final long serialVersionUID = 896735045L;

    public static final QCardTransaction cardTransaction = new QCardTransaction("cardTransaction");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final StringPath cardNumber = createString("cardNumber");

    public final EnumPath<CardTransactionStatus> cardTransactionStatus = createEnum("cardTransactionStatus", CardTransactionStatus.class);

    public final EnumPath<CardTransactionType> cardTransactionType = createEnum("cardTransactionType", CardTransactionType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath merchantAddress = createString("merchantAddress");

    public final StringPath merchantId = createString("merchantId");

    public final StringPath merchantName = createString("merchantName");

    public final StringPath originalMerchantId = createString("originalMerchantId");

    public final DateTimePath<java.time.LocalDateTime> transactionAt = createDateTime("transactionAt", java.time.LocalDateTime.class);

    public QCardTransaction(String variable) {
        super(CardTransaction.class, forVariable(variable));
    }

    public QCardTransaction(Path<? extends CardTransaction> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCardTransaction(PathMetadata metadata) {
        super(CardTransaction.class, metadata);
    }

}

