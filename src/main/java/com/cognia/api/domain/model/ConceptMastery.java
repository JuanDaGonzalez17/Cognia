package com.cognia.api.domain.model;

import java.util.UUID;

public record ConceptMastery(UUID conceptId, double level, int totalEvaluations, int totalCorrect) {
	public ConceptMastery {
		if (conceptId == null) throw new IllegalArgumentException("Concept is required");
		if (!Double.isFinite(level) || level < 0 || level > 1) throw new IllegalArgumentException("Mastery must be between 0 and 1");
		if (totalEvaluations < 0 || totalCorrect < 0 || totalCorrect > totalEvaluations) throw new IllegalArgumentException("Invalid evidence counters");
	}
}
