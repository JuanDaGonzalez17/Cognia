package com.cognia.api;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Configuración de Testcontainers compartida por todos los tests de integración.
 * Declara el contenedor como Spring @Bean para que @ServiceConnection funcione
 * y el ciclo de vida sea gestionado por el contexto de Spring (reutilización
 * del contenedor entre tests).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer("postgres:16-alpine")
				.withInitScript("db/migration/schema.sql");
	}
}
