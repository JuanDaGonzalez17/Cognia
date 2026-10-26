package com.cognia.api.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ReviewSession(UUID id, UUID studentId, UUID subjectId, Instant startedAt, Instant finishedAt,
		int reviewed, int remembered, int forgotten) {
	public ReviewSession {
		if (studentId == null || subjectId == null || startedAt == null || reviewed < 0 || remembered < 0 || forgotten < 0 || remembered + forgotten != reviewed) throw new IllegalArgumentException("Invalid review session");
	}
	public boolean inProgress() { return finishedAt == null; }
}
