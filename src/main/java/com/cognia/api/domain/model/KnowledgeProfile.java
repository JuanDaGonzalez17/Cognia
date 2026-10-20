package com.cognia.api.domain.model;

import java.util.List;
import java.util.UUID;

public record KnowledgeProfile(UUID id, UUID studentId, UUID subjectId, boolean diagnosticCompleted, List<ConceptMastery> masteries) {
	public KnowledgeProfile {
		if (id == null || studentId == null || subjectId == null) throw new IllegalArgumentException("Profile identifiers are required");
		masteries = List.copyOf(masteries);
	}
}
