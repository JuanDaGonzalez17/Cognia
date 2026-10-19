package com.cognia.api.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrerequisiteJpaRepository extends JpaRepository<PrerequisiteJpaEntity, PrerequisiteJpaEntity.PrerequisiteId> {
	List<PrerequisiteJpaEntity> findAllByConceptId(UUID conceptId);

	List<PrerequisiteJpaEntity> findAllBySubjectId(UUID subjectId);
}
