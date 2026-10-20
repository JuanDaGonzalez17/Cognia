package com.cognia.api.domain.model;
import java.util.Objects;
import java.util.UUID;
public record ActivityOption(UUID id, String text, boolean correct, String specificFeedback) {
	public ActivityOption { Objects.requireNonNull(id); if (text == null || text.isBlank()) throw new IllegalArgumentException("Option text is required"); }
}
