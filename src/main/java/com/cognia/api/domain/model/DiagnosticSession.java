package com.cognia.api.domain.model;
import java.time.Instant; import java.util.UUID;
public record DiagnosticSession(UUID id, UUID studentId, UUID subjectId, String state, Double score, Instant closedAt) {}
