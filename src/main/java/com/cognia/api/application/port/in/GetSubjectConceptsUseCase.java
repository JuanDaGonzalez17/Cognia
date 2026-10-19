package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.Concept;
import java.util.List;
import java.util.UUID;

public interface GetSubjectConceptsUseCase {
	List<Concept> getConcepts(UUID subjectId);
}
