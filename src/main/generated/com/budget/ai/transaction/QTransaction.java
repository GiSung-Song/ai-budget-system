package com.budget.ai.transaction;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTransaction is a Querydsl query type for Transaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTransaction extends EntityPathBase<Transaction> {

    private static final long serialVersionUID = 857896708L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTransaction transaction = new QTransaction("transaction");

    public final com.budget.ai.config.QBaseTimeEntity _super = new com.budget.ai.config.QBaseTimeEntity(this);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final com.budget.ai.card.QCard card;

    public final com.budget.ai.category.QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath merchantAddress = createString("merchantAddress");

    public final StringPath merchantId = createString("merchantId");

    public final StringPath merchantName = createString("merchantName");

    public final StringPath originalMerchantId = createString("originalMerchantId");

    public final DateTimePath<java.time.LocalDateTime> transactionAt = createDateTime("transactionAt", java.time.LocalDateTime.class);

    public final EnumPath<TransactionStatus> transactionStatus = createEnum("transactionStatus", TransactionStatus.class);

    public final EnumPath<TransactionType> transactionType = createEnum("transactionType", TransactionType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.budget.ai.user.QUser user;

    public QTransaction(String variable) {
        this(Transaction.class, forVariable(variable), INITS);
    }

    public QTransaction(Path<? extends Transaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTransaction(PathMetadata metadata, PathInits inits) {
        this(Transaction.class, metadata, inits);
    }

    public QTransaction(Class<? extends Transaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.card = inits.isInitialized("card") ? new com.budget.ai.card.QCard(forProperty("card"), inits.get("card")) : null;
        this.category = inits.isInitialized("category") ? new com.budget.ai.category.QCategory(forProperty("category")) : null;
        this.user = inits.isInitialized("user") ? new com.budget.ai.user.QUser(forProperty("user")) : null;
    }

}

