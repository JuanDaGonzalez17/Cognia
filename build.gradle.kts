plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.cognia"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		// Java 17 LTS: versión instalada en la máquina de desarrollo.
		// Si el equipo instala Java 21 o 25, actualizar este valor.
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// ── Web y validación ──────────────────────────────────────────────────────
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// ── Persistencia relacional ───────────────────────────────────────────────
	// spring-boot-starter-data-jpa: confirmado en SB 4.1.1 POM de Maven Central
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// PostgreSQL JDBC driver (versión gestionada por Spring Boot 4.1.1).
	runtimeOnly("org.postgresql:postgresql")

	// ── Testing ───────────────────────────────────────────────────────────────
	// Starters de test modulares de Spring Boot 4
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	// Testcontainers PostgreSQL y soporte JUnit Jupiter (gestionados por SB BOM).
	testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
