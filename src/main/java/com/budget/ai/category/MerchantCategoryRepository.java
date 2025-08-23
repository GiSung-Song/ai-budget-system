package com.budget.ai.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantCategoryRepository extends JpaRepository<MerchantCategory, Long> {

    @Query("""
        SELECT mc
        FROM MerchantCategory mc
        JOIN FETCH mc.category c
        WHERE :merchantName LIKE CONCAT('%', mc.merchantName, '%') 
    """)
    Optional<MerchantCategory> findByMerchantNameLike(@Param("merchantName") String merchantName);
}
