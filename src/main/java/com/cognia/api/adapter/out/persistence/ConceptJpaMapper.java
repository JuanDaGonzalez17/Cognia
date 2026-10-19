package com.cognia.api.adapter.out.persistence;

import com.cognia.api.domain.model.Concept;

public class ConceptJpaMapper {
	public Concept toDomain(ConceptJpaEntity entity) {
		return new Concept(entity.getId(), entity.getSubjectId(), entity.getCode(), entity.getName(), entity.getDescription(),
				entity.getLevel(), entity.getSuggestedOrder(), entity.isActive());
	}

	public ConceptJpaEntity toEntity(Concept concept) {
		return new ConceptJpaEntity(concept.id(), concept.subjectId(), concept.code(), concept.name(), concept.description(),
				concept.level(), concept.order(), concept.active());
	}
}
