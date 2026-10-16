package com.cognia.api.domain.model;

import java.util.List;
import java.util.Objects;

public record Subject(String id, String name, String description, List<Concept> concepts) {

	public Subject {
		requireText(id, "Subject id");
		requireText(name, "Subject name");
		requireText(description, "Subject description");
		concepts = List.copyOf(Objects.requireNonNull(concepts, "Subject concepts must not be null"));
	}

	private static void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
	}
}
