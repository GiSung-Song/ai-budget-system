package com.budget.ai.report;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = -1589448156L;

    public static final QReport report = new QReport("report");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final StringPath notificationMessage = createString("notificationMessage");

    public final StringPath reportMessage = createString("reportMessage");

    public final DatePath<java.time.LocalDate> reportMonth = createDate("reportMonth", java.time.LocalDate.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QReport(String variable) {
        super(Report.class, forVariable(variable));
    }

    public QReport(Path<? extends Report> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReport(PathMetadata metadata) {
        super(Report.class, metadata);
    }

}

