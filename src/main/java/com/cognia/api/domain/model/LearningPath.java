package com.cognia.api.domain.model;

import java.util.List;
import java.util.UUID;

public record LearningPath(UUID id, UUID studentId, UUID subjectId, int version, State state, List<PathStep> steps) {
	public enum State { ACTIVA, COMPLETADA, RECALCULADA }
	public LearningPath {
		if (studentId == null || subjectId == null || version < 1 || state == null) throw new IllegalArgumentException("Invalid learning path");
		steps = List.copyOf(steps);
		if (state == State.COMPLETADA && steps.stream().anyMatch(s -> s.state() != PathStep.State.DOMINADO)) throw new IllegalArgumentException("Completed path requires all steps mastered");
	}
	public LearningPath recalculate() {
		if (state != State.ACTIVA) throw new IllegalStateException("Only an active path can be recalculated");
		return new LearningPath(id, studentId, subjectId, version, State.RECALCULADA, steps);
	}
	public LearningPath withSteps(List<PathStep> updated) {
		return new LearningPath(id, studentId, subjectId, version,
			updated.stream().allMatch(s -> s.state() == PathStep.State.DOMINADO) ? State.COMPLETADA : state, updated);
	}
}
