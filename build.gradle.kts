plugins {
	java
	id("org.springframework.boot") version "3.4.8"
	id("io.spring.dependency-management") version "1.1.7"
}

val querydslVersion = "5.0.0"
val generatedDir = "src/main/generated"

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

	implementation("io.github.resilience4j:resilience4j-ratelimiter:2.2.0")

	// 검증
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// webflux
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// AOP
	implementation("org.springframework.boot:spring-boot-starter-aop")

	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("me.paulschwarz:spring-dotenv:3.0.0")

	// QueryDSL
	implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")

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

	// flyway
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")

	// 통합 테스트용 Testcontainers
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:mysql:1.17.6")

	// RestAssured
	testImplementation("io.rest-assured:rest-assured")
	testImplementation("io.rest-assured:json-path")
	testImplementation("io.rest-assured:json-schema-validator")

	testImplementation("org.springframework.batch:spring-batch-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
	options.generatedSourceOutputDirectory.set(file(generatedDir))
	options.compilerArgs.add("-parameters")
}

sourceSets {
	named("main") {
		java.srcDir(generatedDir)
	}
}

tasks.named<Delete>("clean") {
	delete(file(generatedDir))
}

tasks.withType<Test> {
	useJUnitPlatform()
}
