package com.cognia.api.domain.model;

import java.time.Instant;
import java.util.UUID;

public record GlobalExam(UUID id, UUID studentId, UUID subjectId, int attempt, String state, Instant finishedAt, Double score, Boolean passed) {
	public GlobalExam {
		if (studentId == null || subjectId == null || attempt < 1 || state == null) throw new IllegalArgumentException("Invalid global exam");
	}
}
