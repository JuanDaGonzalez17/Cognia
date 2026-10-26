package com.cognia.api.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ReviewProgress(UUID id, UUID studentId, UUID cardId, UUID conceptId, double easeFactor,
		int intervalDays, int repetitions, int totalLapses, int consecutiveLapses, Instant lastReviewedAt,
		Instant nextReviewAt, State state) {
	public enum State { APRENDIENDO, REPASANDO, REAPRENDIENDO }
	public ReviewProgress {
		if (studentId == null || cardId == null || conceptId == null || state == null || nextReviewAt == null || !Double.isFinite(easeFactor) || easeFactor < 1.30 || intervalDays < 0 || repetitions < 0 || totalLapses < 0 || consecutiveLapses < 0 || consecutiveLapses > totalLapses) throw new IllegalArgumentException("Invalid review progress");
	}
	public ReviewProgress withSchedule(double ef, int interval, int repetitions, int lapses, int consecutive, Instant at, State nextState) {
		return new ReviewProgress(id, studentId, cardId, conceptId, ef, interval, repetitions, lapses, consecutive, at, at.plusSeconds(interval * 86400L), nextState);
	}
	public ReviewProgress resetConsecutiveLapses() { return new ReviewProgress(id, studentId, cardId, conceptId, easeFactor, intervalDays, repetitions, totalLapses, 0, lastReviewedAt, nextReviewAt, state); }
}
