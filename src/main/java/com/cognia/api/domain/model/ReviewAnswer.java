package com.cognia.api.domain.model;

import java.util.UUID;

public record ReviewAnswer(UUID id, UUID sessionId, UUID progressId, int grade, int previousInterval, int nextInterval, double nextEaseFactor) { }
