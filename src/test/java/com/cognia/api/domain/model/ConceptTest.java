package com.cognia.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ConceptTest {

	@Test
	void rejectsLevelOutsideSchemaRange() {
		assertThrows(IllegalArgumentException.class, () -> concept(11));
	}

	@Test
	void keepsValidConceptValues() {
		var concept = concept(3);

		assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), concept.id());
		assertEquals(3, concept.level());
	}

	private Concept concept(int level) {
		return new Concept(UUID.fromString("00000000-0000-0000-0000-000000000001"),
				UUID.fromString("00000000-0000-0000-0000-000000000010"), "C1", "name", "description", level, 0, true);
	}
}
