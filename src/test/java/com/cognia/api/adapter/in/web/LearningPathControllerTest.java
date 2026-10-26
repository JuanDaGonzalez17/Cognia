package com.cognia.api.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.out.DiagnosticRepository;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql", "/db/seed-actividades.sql", "/db/seed-diagnostico.sql", "/db/seed-evaluacion-corta.sql"})
class LearningPathControllerTest {
	private static final String SUBJECT = "10000000-0000-0000-0000-000000000001";
	private static final String FIRST_CONCEPT = "20000000-0000-0000-0000-000000000001";
	private static final String CORRECT_OPTION = "52000000-0000-0000-0000-000000000001";
	private static final String THIRD_EVALUATION_ACTIVITY = "40000000-0000-0000-0000-000000000009";
	private static final String THIRD_EVALUATION_CORRECT_OPTION = "50000000-0000-0000-0000-000000000009";
	private static final String MULTIPLE_CHOICE_ACTIVITY = "42000000-0000-0000-0000-000000000001";
	private static final String SHORT_ANSWER_ACTIVITY = "42000000-0000-0000-0000-000000000002";

	@Autowired private MockMvc mvc;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private DiagnosticRepository diagnostics;

	private String student;

	@BeforeEach
	void createIsolatedStudentWithCompletedDiagnostic() {
		student = UUID.randomUUID().toString();
		jdbc.update("INSERT INTO estudiante (id, nombre_completo, correo, password_hash, activo) VALUES (?, ?, ?, ?, TRUE)",
				UUID.fromString(student), "Estudiante de integración", student + "@example.invalid", "FAKE_HASH_FOR_TESTS");
		var profileId = diagnostics.ensureProfile(UUID.fromString(student), UUID.fromString(SUBJECT));
		diagnostics.completeProfile(profileId);
	}

	@Test
	void generatePathReturnsCreatedOrOk() throws Exception {
		mvc.perform(post("/api/v1/subjects/{subjectId}/learning-path", SUBJECT).header("X-Student-Id", student))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.studentId").value(student))
				.andExpect(jsonPath("$.version").value(1))
				.andExpect(jsonPath("$.steps.length()").value(4));
	}

	@Test
	void getNextStepReturnsAvailableStep() throws Exception {
		mvc.perform(post("/api/v1/subjects/{subjectId}/learning-path", SUBJECT).header("X-Student-Id", student))
				.andExpect(status().isOk());

		mvc.perform(get("/api/v1/subjects/{subjectId}/learning-path/next", SUBJECT).header("X-Student-Id", student))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.step.state").value("DISPONIBLE"))
				.andExpect(jsonPath("$.step.conceptId").value(FIRST_CONCEPT))
				.andExpect(jsonPath("$.activities.length()").value(2));
	}

	@Test
	void evaluationCycleCompletesSuccessfully() throws Exception {
		var path = generatePath();
		var firstStepId = firstStepId(path);
		var started = mvc.perform(post("/api/v1/subjects/{subjectId}/path-steps/{stepId}/short-evaluations", SUBJECT, firstStepId)
				.header("X-Student-Id", student))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activities.length()").value(3))
			.andReturn();
		var sessionMatcher = Pattern.compile("\\\"session\\\":\\{\\\"id\\\":\\\"([^\\\"]+)\\\"").matcher(started.getResponse().getContentAsString());
		if (!sessionMatcher.find()) throw new AssertionError("La respuesta no contiene el ID de sesión");
		var sessionId = sessionMatcher.group(1);

		answer(sessionId, MULTIPLE_CHOICE_ACTIVITY, "{\"optionId\":\"" + CORRECT_OPTION + "\",\"seconds\":12}");
		answer(sessionId, SHORT_ANSWER_ACTIVITY, "{\"answer\":\"5x^3/3\",\"seconds\":18}");
		answer(sessionId, THIRD_EVALUATION_ACTIVITY, "{\"optionId\":\"" + THIRD_EVALUATION_CORRECT_OPTION + "\",\"seconds\":10}");

		mvc.perform(post("/api/v1/short-evaluations/{sessionId}/finish", sessionId).header("X-Student-Id", student))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.approved").value(true))
				.andExpect(jsonPath("$.stepState").value("DOMINADO"));
	}

	@Test
	void rejectsEvaluationWhenStepNotAvailableReturnsConflict() throws Exception {
		var path = generatePath();
		var blockedStepMatcher = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\",\\\"conceptId\\\":\\\"[^\\\"]+\\\",\\\"order\\\":\\d+,\\\"state\\\":\\\"BLOQUEADO\\\"").matcher(path);
		assertTrue(blockedStepMatcher.find(), "La ruta debe contener un paso bloqueado para este escenario");
		var blockedStepId = blockedStepMatcher.group(1);

		mvc.perform(post("/api/v1/subjects/{subjectId}/path-steps/{stepId}/short-evaluations", SUBJECT, blockedStepId)
				.header("X-Student-Id", student))
			.andExpect(status().isConflict())
			.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
			.andExpect(jsonPath("$.title").value("Diagnostic conflict"));
	}

	private String generatePath() throws Exception {
		return mvc.perform(post("/api/v1/subjects/{subjectId}/learning-path", SUBJECT).header("X-Student-Id", student))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	}

	private String firstStepId(String pathJson) throws Exception {
		var matcher = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\",\\\"conceptId\\\":\\\"" + FIRST_CONCEPT + "\\\"").matcher(pathJson);
		if (!matcher.find()) throw new AssertionError("La ruta generada no contiene el concepto inicial esperado");
		return matcher.group(1);
	}

	private void answer(String sessionId, String activityId, String body) throws Exception {
		mvc.perform(post("/api/v1/short-evaluations/{sessionId}/answers/{activityId}", sessionId, activityId)
				.header("X-Student-Id", student)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk());
	}
}
