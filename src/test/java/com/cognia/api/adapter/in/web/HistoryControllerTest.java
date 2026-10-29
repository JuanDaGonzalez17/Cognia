package com.cognia.api.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.cognia.api.TestcontainersConfiguration;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql", "/db/seed-actividades.sql", "/db/seed-diagnostico.sql", "/db/seed-evaluacion-corta.sql", "/db/seed-repaso-examen.sql"})
class HistoryControllerTest {
	private static final UUID STUDENT = UUID.fromString("30000000-0000-0000-0000-000000000001");
	private static final UUID OTHER = UUID.fromString("30000000-0000-0000-0000-000000000099");
	private static final UUID SUBJECT = UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final UUID CONCEPT = UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID CARD = UUID.fromString("70000000-0000-0000-0000-000000000001");
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;

	@BeforeEach void clearQueryFixtures() {
		jdbc.update("delete from sesion_repaso where estudiante_id in (?,?)", STUDENT, OTHER);
		jdbc.update("delete from progreso_repaso where estudiante_id in (?,?)", STUDENT, OTHER);
		jdbc.update("delete from sesion_evaluacion where estudiante_id in (?,?)", STUDENT, OTHER);
		jdbc.update("delete from estudiante where id=?", OTHER);
	}

	@Test @WithMockStudent("30000000-0000-0000-0000-000000000001")
	void returnsExpectedShapesAndOnlyAuthenticatedStudentsRows() throws Exception {
		jdbc.update("insert into estudiante(id,nombre_completo,correo,password_hash) values (?,?,?,?)", OTHER, "Otro", "otro-history@test.local", "x");
		insertCompletedEvaluation(STUDENT, "DIAGNOSTICO", null, 80d, null, "2026-10-27T10:00:00Z");
		insertCompletedEvaluation(OTHER, "DIAGNOSTICO", null, 90d, null, "2026-10-28T10:00:00Z");
		insertReview(STUDENT, "2026-10-27T11:00:00Z");
		insertReview(OTHER, "2026-10-28T11:00:00Z");
		insertProgress(STUDENT, Instant.now().plusSeconds(3600));
		insertProgress(OTHER, Instant.now().plusSeconds(1800));
		mvc.perform(get("/api/v1/subjects/{subject}/history", SUBJECT).header("X-Student-Id", STUDENT))
			.andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("REPASO"))
			.andExpect(jsonPath("$[1].conceptName").value("Diagnóstico inicial"))
			.andExpect(jsonPath("$[1].passed").doesNotExist()).andExpect(jsonPath("$[1].attempt").value(1))
			.andExpect(jsonPath("$[2]").doesNotExist());
		mvc.perform(get("/api/v1/subjects/{subject}/reviews/upcoming", SUBJECT).header("X-Student-Id", STUDENT))
			.andExpect(status().isOk()).andExpect(jsonPath("$[0].conceptName").exists())
			.andExpect(jsonPath("$[0].intervalDays").value(2)).andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test void endpointsRequireAuthentication() throws Exception {
		mvc.perform(get("/api/v1/subjects/{subject}/history", SUBJECT)).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/subjects/{subject}/reviews/upcoming", SUBJECT)).andExpect(status().isUnauthorized());
	}

	@Test @WithMockStudent("30000000-0000-0000-0000-000000000001")
	void missingSubjectIsNotFoundAndExistingSubjectWithoutDataReturnsEmptyArrays() throws Exception {
		mvc.perform(get("/api/v1/subjects/{subject}/history", UUID.randomUUID()).header("X-Student-Id", STUDENT)).andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/subjects/{subject}/reviews/upcoming", UUID.randomUUID()).header("X-Student-Id", STUDENT)).andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/subjects/{subject}/history", SUBJECT).header("X-Student-Id", STUDENT)).andExpect(status().isOk()).andExpect(content().json("[]"));
		mvc.perform(get("/api/v1/subjects/{subject}/reviews/upcoming", SUBJECT).header("X-Student-Id", STUDENT)).andExpect(status().isOk()).andExpect(content().json("[]"));
	}

	private void insertCompletedEvaluation(UUID student, String type, UUID concept, double score, Boolean passed, String date) {
		jdbc.update("insert into sesion_evaluacion(id,estudiante_id,tipo_evaluacion,asignatura_id,concepto_id,intento_numero,fecha_finalizacion,puntaje_obtenido,aprobada,estado) values (?,?,?,?,?,1,?,?,?,'COMPLETADA')",
			UUID.randomUUID(), student, type, SUBJECT, concept, Timestamp.from(Instant.parse(date)), score, passed);
	}
	private void insertReview(UUID student, String date) {
		jdbc.update("insert into sesion_repaso(id,estudiante_id,asignatura_id,fecha_inicio,fecha_fin,total_tarjetas_revisadas,total_recordadas,total_olvidadas) values (?,?,?,?,?,2,1,1)",
			UUID.randomUUID(), student, SUBJECT, Timestamp.from(Instant.parse(date).minusSeconds(60)), Timestamp.from(Instant.parse(date)));
	}
	private void insertProgress(UUID student, Instant date) {
		jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,intervalo_dias,fecha_proximo_repaso) values (?,?,?,?,?)", student, CARD, CONCEPT, 2, Timestamp.from(date));
	}
}
