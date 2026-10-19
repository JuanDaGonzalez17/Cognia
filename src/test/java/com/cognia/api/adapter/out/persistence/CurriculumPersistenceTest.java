package com.cognia.api.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.out.ConceptRepository;
import com.cognia.api.application.port.out.PrerequisiteRepository;
import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Prerequisite;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Sql("/db/seed-calculo-integral.sql")
class CurriculumPersistenceTest {
	private static final UUID SUBJECT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final UUID FIRST_CONCEPT_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID SECOND_CONCEPT_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private ConceptRepository conceptRepository;
	@Autowired
	private PrerequisiteRepository prerequisiteRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void readsSeededSubjectAndConceptsThroughExplicitMappers() {
		var subject = subjectRepository.findById(SUBJECT_ID).orElseThrow();
		var concepts = conceptRepository.findBySubjectId(SUBJECT_ID);

		assertEquals("calculo-integral", subject.code());
		assertEquals(4, concepts.size());
		assertEquals("C0.1", conceptRepository.findById(FIRST_CONCEPT_ID).orElseThrow().code());
	}

	@Test
	void databaseRejectsPrerequisiteAcrossSubjects() {
		var otherSubjectId = UUID.fromString("10000000-0000-0000-0000-000000000002");
		var otherConceptId = UUID.fromString("20000000-0000-0000-0000-000000000005");
		jdbcTemplate.update("INSERT INTO asignatura (id, codigo, nombre) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
				otherSubjectId, "other-subject", "Otra asignatura");
		jdbcTemplate.update("INSERT INTO concepto (id, asignatura_id, codigo, nombre, orden_sugerido) " +
				"VALUES (?, ?, ?, ?, 0) ON CONFLICT (id) DO NOTHING",
				otherConceptId, otherSubjectId, "X1", "Concepto externo");

		assertThrows(DataIntegrityViolationException.class, () -> prerequisiteRepository.save(
				new Prerequisite(SUBJECT_ID, FIRST_CONCEPT_ID, otherConceptId)));
		assertTrue(prerequisiteRepository.findByConceptId(FIRST_CONCEPT_ID).isEmpty());
	}
}
