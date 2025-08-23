package com.budget.ai.category;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMerchantCategory is a Querydsl query type for MerchantCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMerchantCategory extends EntityPathBase<MerchantCategory> {

    private static final long serialVersionUID = 600980428L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMerchantCategory merchantCategory = new QMerchantCategory("merchantCategory");

    public final QCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath merchantName = createString("merchantName");

    public QMerchantCategory(String variable) {
        this(MerchantCategory.class, forVariable(variable), INITS);
    }

    public QMerchantCategory(Path<? extends MerchantCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMerchantCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMerchantCategory(PathMetadata metadata, PathInits inits) {
        this(MerchantCategory.class, metadata, inits);
    }

    public QMerchantCategory(Class<? extends MerchantCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category")) : null;
    }

}

