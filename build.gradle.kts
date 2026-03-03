plugins {
	java
	id("org.springframework.boot") version "3.5.9"
	id("io.spring.dependency-management") version "1.1.7"
	id("checkstyle")
	id("jacoco")
	id("org.flywaydb.flyway") version "9.22.3"
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
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:testcontainers:1.19.8")
	testImplementation("org.testcontainers:junit-jupiter:1.19.8")
	testImplementation("org.testcontainers:postgresql:1.19.8")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
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
	dependsOn("checkstyleMain", "checkstyleTest", "build", "test", "jacocoTestReport")
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

jacoco {
	toolVersion = "0.8.11"
	reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		xml.required = true
		html.required = true
		csv.required = false
	}

	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it).apply {
				exclude(
					"**/MeetingAssistantBotApplication.class",
					"**/config/**",
					"**/dto/**",
					"**/model/**",
					"**/mapper/**"
				)
			}
		})
	)
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.jacocoTestReport)

	violationRules {
		rule {
			limit {
				minimum = BigDecimal.valueOf(0.8)
			}
		}
	}
}

tasks.register("jacocoInfo") {
	group = "help"
	description = "Show JaCoCo configuration info"
	doLast {
		println("📊 JaCoCo Configuration:")
		println("   • Tool version: ${jacoco.toolVersion}")
		println("   • Reports directory: ${jacoco.reportsDirectory.get()}")
		println("\n🔧 Available tasks:")
		println("   • ./gradlew test              - Run tests")
		println("   • ./gradlew jacocoTestReport  - Generate coverage report")
		println("   • ./gradlew jacocoTestCoverageVerification - Check coverage limits")
	}
}

flyway {
	url = "jdbc:postgresql://localhost:5432/meeting_bot_db"
	user = "postgres"
	password = "bot_password"
	schemas = arrayOf("public")
	locations = arrayOf("filesystem:src/main/resources/db/migration")
}