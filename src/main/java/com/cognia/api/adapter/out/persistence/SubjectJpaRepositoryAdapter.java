package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectJpaRepositoryAdapter implements SubjectRepository {
	private final SubjectJpaRepository repository;
	private final SubjectJpaMapper mapper = new SubjectJpaMapper();

	public SubjectJpaRepositoryAdapter(SubjectJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<Subject> findAll() {
		return repository.findAll().stream().map(mapper::toDomain).toList();
	}

	@Override
	public Optional<Subject> findById(UUID id) {
		return repository.findById(id).map(mapper::toDomain);
	}
}
