package com.cognia.api.domain.model;

import java.util.UUID;

public record PathStep(UUID id, UUID conceptId, int order, State state) {
	public enum State { BLOQUEADO, DISPONIBLE, EN_PROGRESO, REFUERZO, DOMINADO }
	public PathStep {
		if (conceptId == null || state == null || order < 0) throw new IllegalArgumentException("Invalid path step");
	}
	public PathStep transitionTo(State next) {
		boolean valid = switch (state) {
			case BLOQUEADO -> next == State.DISPONIBLE;
			case DISPONIBLE -> next == State.EN_PROGRESO || next == State.DOMINADO || next == State.BLOQUEADO;
			case EN_PROGRESO -> next == State.REFUERZO || next == State.DOMINADO || next == State.BLOQUEADO;
			case REFUERZO -> next == State.EN_PROGRESO || next == State.REFUERZO || next == State.DOMINADO || next == State.BLOQUEADO;
			case DOMINADO -> next == State.EN_PROGRESO || next == State.BLOQUEADO || next == State.REFUERZO;
		};
		if (!valid) throw new InvalidPathTransitionException("Cannot transition path step from " + state + " to " + next);
		return new PathStep(id, conceptId, order, next);
	}
}
