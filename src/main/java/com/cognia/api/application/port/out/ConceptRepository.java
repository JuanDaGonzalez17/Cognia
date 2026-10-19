package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.Concept;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConceptRepository {
	List<Concept> findBySubjectId(UUID subjectId);

	Optional<Concept> findById(UUID id);
}
