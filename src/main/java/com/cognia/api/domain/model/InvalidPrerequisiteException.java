package com.cognia.api.domain.model;

public class InvalidPrerequisiteException extends RuntimeException {
	public enum Reason {
		INVALID_REQUEST,
		GRAPH_CONFLICT
	}

	private final Reason reason;

	public InvalidPrerequisiteException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public Reason reason() {
		return reason;
	}
}
