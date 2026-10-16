package com.cognia.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConceptTest {

	@Test
	void rejectsDifficultyOutsideSupportedRange() {
		assertThrows(IllegalArgumentException.class,
				() -> new Concept("id", "name", "description", 6));
	}

	@Test
	void keepsValidConceptValues() {
		var concept = new Concept("id", "name", "description", 3);

		assertEquals("id", concept.id());
		assertEquals(3, concept.difficulty());
	}
}
