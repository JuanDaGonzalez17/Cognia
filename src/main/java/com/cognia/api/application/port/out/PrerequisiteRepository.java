package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.Prerequisite;
import java.util.List;
import java.util.UUID;

public interface PrerequisiteRepository {
	List<Prerequisite> findByConceptId(UUID conceptId);

	List<Prerequisite> findBySubjectId(UUID subjectId);

	Prerequisite save(Prerequisite prerequisite);
}
