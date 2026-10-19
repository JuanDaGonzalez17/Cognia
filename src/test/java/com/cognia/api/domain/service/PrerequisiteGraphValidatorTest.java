package com.cognia.api.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cognia.api.domain.model.Concept;
import com.cognia.api.domain.model.InvalidPrerequisiteException;
import com.cognia.api.domain.model.InvalidPrerequisiteException.Reason;
import com.cognia.api.domain.model.Prerequisite;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PrerequisiteGraphValidatorTest {
	private static final UUID SUBJECT = id(100);
	private final PrerequisiteGraphValidator validator = new PrerequisiteGraphValidator();

	@Test
	void rejectsThreeConceptCycle() {
		var a = concept(1, SUBJECT);
		var b = concept(2, SUBJECT);
		var c = concept(3, SUBJECT);
		var graph = List.of(edge(1, 2), edge(2, 3));

		var error = assertThrows(InvalidPrerequisiteException.class, () -> validator.validate(c, a, graph));

		assertEquals(Reason.GRAPH_CONFLICT, error.reason());
	}

	@Test
	void rejectsSelfPrerequisite() {
		var concept = concept(1, SUBJECT);

		var error = assertThrows(InvalidPrerequisiteException.class,
				() -> validator.validate(concept, concept, List.of()));

		assertEquals(Reason.INVALID_REQUEST, error.reason());
	}

	@Test
	void rejectsPrerequisiteFromDifferentSubject() {
		var concept = concept(1, SUBJECT);
		var prerequisite = concept(2, id(200));

		var error = assertThrows(InvalidPrerequisiteException.class,
				() -> validator.validate(concept, prerequisite, List.of()));

		assertEquals(Reason.INVALID_REQUEST, error.reason());
	}

	@Test
	void acceptsValidGraph() {
		assertDoesNotThrow(() -> validator.validate(concept(2, SUBJECT), concept(1, SUBJECT), List.of()));
	}

	private static Concept concept(int id, UUID subjectId) {
		return new Concept(id(id), subjectId, "C" + id, "Concept " + id, null, 1, id, true);
	}

	private static Prerequisite edge(int conceptId, int prerequisiteId) {
		return new Prerequisite(SUBJECT, id(conceptId), id(prerequisiteId));
	}

	private static UUID id(int value) {
		return new UUID(0, value);
	}
}
