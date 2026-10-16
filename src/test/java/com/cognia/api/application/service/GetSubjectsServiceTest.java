package com.cognia.api.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetSubjectsServiceTest {

	@Test
	void delegatesSubjectQueriesToOutputPort() {
		var subject = new Subject("subject", "Subject", "Description", List.of());
		SubjectRepository repository = new SubjectRepository() {
			@Override
			public List<Subject> findAll() {
				return List.of(subject);
			}

			@Override
			public Optional<Subject> findById(String id) {
				return Optional.of(subject);
			}
		};
		var service = new GetSubjectsService(repository);

		assertEquals(List.of(subject), service.getAll());
		assertEquals(Optional.of(subject), service.getById("subject"));
	}
}
