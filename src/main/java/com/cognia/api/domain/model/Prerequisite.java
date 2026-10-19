package com.cognia.api.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Prerequisite(UUID subjectId, UUID conceptId, UUID prerequisiteId) {
	public Prerequisite {
		Objects.requireNonNull(subjectId, "Prerequisite subject id must not be null");
		Objects.requireNonNull(conceptId, "Concept id must not be null");
		Objects.requireNonNull(prerequisiteId, "Prerequisite id must not be null");
	}
}
