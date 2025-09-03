package com.budget.ai.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.budget.ai.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<Long> getAllUserId() {
        return jpaQueryFactory
                .select(user.id)
                .from(user)
                .fetch();
    }
}
