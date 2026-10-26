package com.cognia.api.domain.model;

import java.util.UUID;

public record GlobalExamSummary(UUID conceptId, int correct, int total) { }
