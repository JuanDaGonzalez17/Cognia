package com.cognia.api.adapter.out.persistence;

import com.cognia.api.domain.model.Prerequisite;

public class PrerequisiteJpaMapper {
	public Prerequisite toDomain(PrerequisiteJpaEntity entity) {
		return new Prerequisite(entity.getSubjectId(), entity.getConceptId(), entity.getPrerequisiteId());
	}

	public PrerequisiteJpaEntity toEntity(Prerequisite prerequisite) {
		return new PrerequisiteJpaEntity(prerequisite.conceptId(), prerequisite.prerequisiteId(), prerequisite.subjectId());
	}
}
