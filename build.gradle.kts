plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("checkstyle")
}

group = "com.antonyukV516"
version = "0.0.1-SNAPSHOT"
description = "Telegram bot for meeting coordination"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
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

val telegrambotsVersion = "6.9.7.1"
val mapstructVersion = "1.5.5.Final"
val testcontainersVersion = "1.19.6"
val testcontainersRedisVersion = "1.4.6"


dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("org.flywaydb:flyway-core")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-database-postgresql")

	implementation("org.telegram:telegrambots-spring-boot-starter:$telegrambotsVersion")

	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	compileOnly("org.projectlombok:lombok")

	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
		exclude(module = "mockito-core")
	}
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
	testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
	testImplementation("org.testcontainers:rabbitmq:$testcontainersVersion")
	testImplementation("com.redis.testcontainers:testcontainers-redis-junit-jupiter:$testcontainersRedisVersion")
	testImplementation("org.awaitility:awaitility:4.2.1")
	testImplementation("org.assertj:assertj-core:3.25.3")
	testImplementation("org.mockito:mockito-core:5.11.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
		showStandardStreams = true
		showCauses = true
		showStackTraces = true
	}
	systemProperty("spring.profiles.active", "test")
}

// Checkstyle configuration
checkstyle {
	toolVersion = "10.14.0"
	configFile = file("config/checkstyle/checkstyle.xml")
	isIgnoreFailures = false
	maxWarnings = 0
}

tasks.checkstyleMain {
	group = "verification"
}

tasks.checkstyleTest {
	group = "verification"
}