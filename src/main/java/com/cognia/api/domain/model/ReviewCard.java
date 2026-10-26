package com.cognia.api.domain.model;

import java.util.UUID;

public record ReviewCard(UUID id, UUID conceptId, String front, String back, String hint) {
	public ReviewCard {
		if (id == null || conceptId == null || front == null || front.isBlank() || back == null || back.isBlank()) throw new IllegalArgumentException("Review card identifiers and text are required");
	}
}
