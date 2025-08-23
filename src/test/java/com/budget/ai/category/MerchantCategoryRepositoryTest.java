package com.budget.ai.category;

import com.budget.ai.testsupport.RepositoryTest;
import com.budget.ai.testsupport.container.TestContainerManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class MerchantCategoryRepositoryTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Autowired
    private MerchantCategoryRepository merchantCategoryRepository;

    @Test
    void 상호명_조회_카페_성공() {
        String merchantName = "스타벅스 강남점";

        MerchantCategory merchantCategory = merchantCategoryRepository.findByMerchantNameLike(merchantName)
                .orElseThrow();

        assertThat(merchantCategory.getMerchantName()).isEqualTo("스타벅스");
        assertThat(merchantCategory.getCategory().getCode()).isEqualTo("CAFE");
    }

    @Test
    void 상호명_조회_마트_성공() {
        String merchantName = "홈플러스 방학점";

        MerchantCategory merchantCategory = merchantCategoryRepository.findByMerchantNameLike(merchantName)
                .orElseThrow();

        assertThat(merchantCategory.getMerchantName()).isEqualTo("홈플러스");
        assertThat(merchantCategory.getCategory().getCode()).isEqualTo("MART");
    }

    @Test
    void 상호명_조회_없음() {
        String merchantName = "옥토퍼스 맛있다 서초역";

        MerchantCategory merchantCategory = merchantCategoryRepository.findByMerchantNameLike(merchantName)
                .orElse(null);

        assertThat(merchantCategory).isNull();
    }

}