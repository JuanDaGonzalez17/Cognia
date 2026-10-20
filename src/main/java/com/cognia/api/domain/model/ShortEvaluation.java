package com.cognia.api.domain.model;
import java.time.Instant; import java.util.*;
public record ShortEvaluation(UUID id, UUID studentId, UUID subjectId, UUID conceptId, UUID pathStepId, int attempt, String state, Instant closedAt, Double score, Boolean passed) {
	public ShortEvaluation { if(studentId==null||subjectId==null||conceptId==null||pathStepId==null||attempt<1)throw new IllegalArgumentException("Evaluation identifiers and attempt are required"); }
}
