package com.cognia.api.adapter.out.persistence;

import com.cognia.api.domain.model.Subject;

public class SubjectJpaMapper {
	public Subject toDomain(SubjectJpaEntity entity) {
		return new Subject(entity.getId(), entity.getCode(), entity.getName(), entity.getDescription(), entity.isActive());
	}

	public SubjectJpaEntity toEntity(Subject subject) {
		return new SubjectJpaEntity(subject.id(), subject.code(), subject.name(), subject.description(), subject.active());
	}
}
