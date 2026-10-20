package com.cognia.api.domain.model;

public record DiagnosticEvidence(int difficulty, boolean correct) {
	public DiagnosticEvidence {
		if (difficulty < 1 || difficulty > 3) throw new IllegalArgumentException("Difficulty must be between 1 and 3");
	}
}
