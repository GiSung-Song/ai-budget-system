plugins {
	java
	id("org.springframework.boot") version "3.4.8"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.budget"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {

	// 스프링 배치
	implementation("org.springframework.boot:spring-boot-starter-batch")

	// JPA
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// 검증
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// AOP
	implementation("org.springframework.boot:spring-boot-starter-aop")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

	compileOnly("org.projectlombok:lombok")

	// MySQL
	runtimeOnly("com.mysql:mysql-connector-j")

	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")


	// 통합 테스트용 Testcontainers
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:mysql:1.17.6")

	testImplementation("org.springframework.batch:spring-batch-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
