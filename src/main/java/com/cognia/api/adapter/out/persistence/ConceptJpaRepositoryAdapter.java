package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.out.ConceptRepository;
import com.cognia.api.domain.model.Concept;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ConceptJpaRepositoryAdapter implements ConceptRepository {
	private final ConceptJpaRepository repository;
	private final ConceptJpaMapper mapper = new ConceptJpaMapper();

	public ConceptJpaRepositoryAdapter(ConceptJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<Concept> findBySubjectId(UUID subjectId) {
		return repository.findAllBySubjectIdOrderBySuggestedOrder(subjectId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Optional<Concept> findById(UUID id) {
		return repository.findById(id).map(mapper::toDomain);
	}
}
