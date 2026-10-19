package com.cognia.api.adapter.out.persistence;

import com.cognia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Smoke test backed by PostgreSQL Testcontainers, never Neon. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SchemaSmokePersistenceTest {
	@Autowired
	private DataSource dataSource;

	@Test
	void schemaScriptCreatesCurriculumTables() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			assertTableExists(connection, "asignatura");
			assertTableExists(connection, "concepto");
			assertTableExists(connection, "prerrequisito");
		}
	}

	private void assertTableExists(Connection connection, String table) throws Exception {
		try (ResultSet result = connection.getMetaData()
				.getTables(null, "public", table, new String[] { "TABLE" })) {
			assertTrue(result.next(), "La tabla '" + table + "' debe existir");
		}
	}
}
