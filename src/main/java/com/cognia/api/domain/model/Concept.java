package com.cognia.api.domain.model;

public record Concept(String id, String name, String description, int difficulty) {

	public Concept {
		requireText(id, "Concept id");
		requireText(name, "Concept name");
		requireText(description, "Concept description");
		if (difficulty < 1 || difficulty > 5) {
			throw new IllegalArgumentException("Concept difficulty must be between 1 and 5");
		}
	}

	private static void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
	}
}
