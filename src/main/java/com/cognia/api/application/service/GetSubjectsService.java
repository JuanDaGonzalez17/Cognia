package com.cognia.api.application.service;

import com.cognia.api.application.port.in.GetSubjectsUseCase;
import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GetSubjectsService implements GetSubjectsUseCase {

	private final SubjectRepository subjectRepository;

	public GetSubjectsService(SubjectRepository subjectRepository) {
		this.subjectRepository = subjectRepository;
	}

	@Override
	public List<Subject> getAll() {
		return subjectRepository.findAll();
	}

	@Override
	public Optional<Subject> getById(String id) {
		return subjectRepository.findById(id);
	}
}
