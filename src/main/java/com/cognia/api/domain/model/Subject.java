package com.cognia.api.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Subject(UUID id, String code, String name, String description, boolean active) {
	public Subject {
		Objects.requireNonNull(id, "Subject id must not be null");
		requireText(code, "Subject code");
		requireText(name, "Subject name");
	}

	private static void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
	}
}
