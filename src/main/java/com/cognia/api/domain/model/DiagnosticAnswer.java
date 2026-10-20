package com.cognia.api.domain.model;
import java.util.UUID;
public record DiagnosticAnswer(UUID id, UUID sessionId, UUID activityId, UUID optionId, String answer, boolean correct, Integer seconds) {}
