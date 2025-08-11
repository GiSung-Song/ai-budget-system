package com.budget.ai;

import com.budget.ai.testsupport.container.TestContainerManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("test")
@SpringBootTest
class AiApplicationTests {

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		TestContainerManager.startRedis();

		TestContainerManager.registerMySQL(registry);
		TestContainerManager.registerRedis(registry);
	}

	@Test
	void contextLoads() {
	}
}