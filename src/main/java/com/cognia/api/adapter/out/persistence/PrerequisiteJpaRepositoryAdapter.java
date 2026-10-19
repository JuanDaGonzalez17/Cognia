package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.out.PrerequisiteRepository;
import com.cognia.api.domain.model.Prerequisite;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PrerequisiteJpaRepositoryAdapter implements PrerequisiteRepository {
	private final PrerequisiteJpaRepository repository;
	private final PrerequisiteJpaMapper mapper = new PrerequisiteJpaMapper();

	public PrerequisiteJpaRepositoryAdapter(PrerequisiteJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<Prerequisite> findByConceptId(UUID conceptId) {
		return repository.findAllByConceptId(conceptId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public List<Prerequisite> findBySubjectId(UUID subjectId) {
		return repository.findAllBySubjectId(subjectId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Prerequisite save(Prerequisite prerequisite) {
		return mapper.toDomain(repository.save(mapper.toEntity(prerequisite)));
	}
}
