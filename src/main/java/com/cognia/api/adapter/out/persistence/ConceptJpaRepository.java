package com.cognia.api.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptJpaRepository extends JpaRepository<ConceptJpaEntity, UUID> {
	List<ConceptJpaEntity> findAllBySubjectIdOrderBySuggestedOrder(UUID subjectId);
}
