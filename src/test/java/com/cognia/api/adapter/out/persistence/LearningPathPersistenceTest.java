package com.cognia.api.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.in.LearningPathUseCases;
import com.cognia.api.application.port.out.DiagnosticRepository;
import com.cognia.api.application.port.out.LearningPathRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql", "/db/seed-actividades.sql", "/db/seed-diagnostico.sql", "/db/seed-evaluacion-corta.sql"})
class LearningPathPersistenceTest {
	private static final UUID SUBJECT = UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final UUID CONCEPT = UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID CORRECT_OPTION = UUID.fromString("52000000-0000-0000-0000-000000000001");
	private static final UUID WRONG_OPTION = UUID.fromString("52000000-0000-0000-0000-000000000002");
	private static final UUID FIRST_EVALUATION_ACTIVITY = UUID.fromString("42000000-0000-0000-0000-000000000001");
	private static final UUID SECOND_EVALUATION_ACTIVITY = UUID.fromString("42000000-0000-0000-0000-000000000002");
	private static final UUID THIRD_EVALUATION_ACTIVITY = UUID.fromString("40000000-0000-0000-0000-000000000009");
	private static final UUID THIRD_CORRECT_OPTION = UUID.fromString("50000000-0000-0000-0000-000000000009");
	private static final UUID THIRD_WRONG_OPTION = UUID.fromString("50000000-0000-0000-0000-000000000010");

	@Autowired private LearningPathUseCases learningPaths;
	@Autowired private LearningPathRepository pathRepository;
	@Autowired private DiagnosticRepository diagnostics;
	@Autowired private JdbcTemplate jdbc;

	private UUID student;

	@BeforeEach
	void createIsolatedStudentWithCompletedDiagnostic() {
		student = UUID.randomUUID();
		jdbc.update("INSERT INTO estudiante (id, nombre_completo, correo, password_hash, activo) VALUES (?, ?, ?, ?, TRUE)",
				student, "Estudiante de integración", student + "@example.invalid", "FAKE_HASH_FOR_TESTS");
		var profileId = diagnostics.ensureProfile(student, SUBJECT);
		diagnostics.completeProfile(profileId);
	}

	@Test
	void generatesAndPersistsInitialLearningPathFromProfile() {
		var path = learningPaths.generate(student, SUBJECT);

		assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM ruta_aprendizaje WHERE id = ? AND estudiante_id = ? AND version = 1 AND estado = 'ACTIVA'", Integer.class, path.id(), student));
		assertEquals(path.steps().size(), jdbc.queryForObject("SELECT count(*) FROM paso_ruta WHERE ruta_id = ?", Integer.class, path.id()));
		assertTrue(path.steps().stream().anyMatch(step -> step.state().name().equals("DISPONIBLE")));
		assertTrue(path.steps().stream().anyMatch(step -> step.state().name().equals("BLOQUEADO")));
	}

	@Test
	void advancesStepStateToDominatedUponApproval() {
		var path = learningPaths.generate(student, SUBJECT);
		var firstStep = path.steps().stream().filter(step -> step.conceptId().equals(CONCEPT)).findFirst().orElseThrow();
		var evaluation = learningPaths.start(student, SUBJECT, firstStep.id());
		learningPaths.answer(student, evaluation.session().id(), FIRST_EVALUATION_ACTIVITY, CORRECT_OPTION, null, 12);
		learningPaths.answer(student, evaluation.session().id(), SECOND_EVALUATION_ACTIVITY, null, "5x^3/3", 18);
		learningPaths.answer(student, evaluation.session().id(), THIRD_EVALUATION_ACTIVITY, THIRD_CORRECT_OPTION, null, 10);

		var result = learningPaths.finish(student, evaluation.session().id());
		var updated = pathRepository.byId(path.id()).orElseThrow();

		assertTrue(Boolean.TRUE.equals(result.session().passed()));
		assertEquals("DOMINADO", jdbc.queryForObject("SELECT estado FROM paso_ruta WHERE id = ?", String.class, firstStep.id()));
		assertTrue(updated.steps().stream().anyMatch(step -> step.state().name().equals("DISPONIBLE")));
	}

	@Test
	void recalculatesRouteWithIncrementedVersionOnRepeatedFailure() {
		var original = learningPaths.generate(student, SUBJECT);
		var firstStep = original.steps().stream().filter(step -> step.conceptId().equals(CONCEPT)).findFirst().orElseThrow();

		finishFailedEvaluation(firstStep.id());
		var result = finishFailedEvaluation(firstStep.id());
		var latest = pathRepository.latest(student, SUBJECT).orElseThrow();

		assertTrue(result.recalculated());
		assertEquals("RECALCULADA", jdbc.queryForObject("SELECT estado FROM ruta_aprendizaje WHERE id = ?", String.class, original.id()));
		assertEquals(2, latest.version());
		assertEquals("REFUERZO", latest.steps().stream().filter(step -> step.conceptId().equals(CONCEPT)).findFirst().orElseThrow().state().name());
	}

	private LearningPathUseCases.EvaluationResult finishFailedEvaluation(UUID stepId) {
		var evaluation = learningPaths.start(student, SUBJECT, stepId);
		learningPaths.answer(student, evaluation.session().id(), FIRST_EVALUATION_ACTIVITY, WRONG_OPTION, null, 12);
		learningPaths.answer(student, evaluation.session().id(), SECOND_EVALUATION_ACTIVITY, null, "respuesta incorrecta", 18);
		learningPaths.answer(student, evaluation.session().id(), THIRD_EVALUATION_ACTIVITY, THIRD_WRONG_OPTION, null, 10);
		return learningPaths.finish(student, evaluation.session().id());
	}
}
