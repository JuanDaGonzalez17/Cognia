package com.cognia.api.domain.model;
import java.util.UUID;
public record PracticeResult(boolean correct, String feedback, String specificFeedback, UUID correctOptionId) { }
