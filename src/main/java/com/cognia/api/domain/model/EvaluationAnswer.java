package com.cognia.api.domain.model;
import java.util.UUID;
public record EvaluationAnswer(UUID id, UUID sessionId, UUID activityId, boolean correct) {}
