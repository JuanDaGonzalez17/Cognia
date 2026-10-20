package com.cognia.api.domain.service;

import com.cognia.api.domain.model.ConceptMastery;
import com.cognia.api.domain.model.DiagnosticEvidence;
import java.util.List;
import java.util.UUID;

public interface MasteryPolicy {
	ConceptMastery estimate(UUID conceptId, List<DiagnosticEvidence> evidence);
	default ConceptMastery incorporate(UUID conceptId, ConceptMastery previous, List<DiagnosticEvidence> evidence) { throw new UnsupportedOperationException("Incremental mastery is not supported"); }
}
