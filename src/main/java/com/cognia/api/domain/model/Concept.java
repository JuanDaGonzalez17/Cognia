package com.cognia.api.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Concept(UUID id, UUID subjectId, String code, String name, String description,
		int level, int order, boolean active) {
	public Concept {
		Objects.requireNonNull(id, "Concept id must not be null");
		Objects.requireNonNull(subjectId, "Concept subject id must not be null");
		requireText(code, "Concept code");
		requireText(name, "Concept name");
		if (level < 0 || level > 10) {
			throw new IllegalArgumentException("Concept level must be between 0 and 10");
		}
		if (order < 0) {
			throw new IllegalArgumentException("Concept order must not be negative");
		}
	}

	private static void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
	}
}
