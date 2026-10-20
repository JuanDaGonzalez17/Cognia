package com.cognia.api.domain.model;
import java.util.UUID;
public record PracticeAttempt(UUID id, UUID studentId, UUID activityId, UUID selectedOptionId, String answer, boolean correct, Integer seconds) { }
