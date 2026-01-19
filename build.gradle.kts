plugins {
	java
	id("org.springframework.boot") version "3.5.9"
	id("io.spring.dependency-management") version "1.1.7"
	id("checkstyle")
}

group = "com.antonyukV516"
version = "0.0.1-SNAPSHOT"
description = "Telegram bot for meeting coordination"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

checkstyle {
	toolVersion = "10.12.5"
	configFile = file("config/checkstyle/checkstyle.xml")
	isIgnoreFailures = false
	maxWarnings = 10
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register("ciCheck") {
	group = "verification"
	description = "Run all checks for CI pipeline"
	dependsOn("checkstyleMain", "checkstyleTest", "build", "test")
}

tasks.register("checkstyleInfo") {
	group = "help"
	description = "Show Checkstyle configuration info"
	doLast {
		println("📋 Checkstyle Configuration:")
		println("   • Tool version: ${checkstyle.toolVersion}")
		println("   • Config file: ${checkstyle.configFile}")
		println("   • Ignore failures: ${checkstyle.isIgnoreFailures}")
		println("   • Max warnings: ${checkstyle.maxWarnings}")
		println("\n🔧 Available tasks:")
		println("   • ./gradlew checkstyleMain    - Check main source code")
		println("   • ./gradlew checkstyleTest    - Check test source code")
		println("   • ./gradlew ciCheck          - Run all CI checks")
		println("   • ./gradlew checkstyleInfo   - Show this info")
	}
}