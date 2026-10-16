package com.cognia.api.adapter.in.web;

import com.cognia.api.application.port.in.GetSubjectsUseCase;
import com.cognia.api.domain.model.Concept;
import com.cognia.api.domain.model.Subject;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

	private final GetSubjectsUseCase getSubjectsUseCase;

	public SubjectController(GetSubjectsUseCase getSubjectsUseCase) {
		this.getSubjectsUseCase = getSubjectsUseCase;
	}

	@GetMapping
	public List<SubjectResponse> getAll() {
		return getSubjectsUseCase.getAll().stream().map(SubjectResponse::from).toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<SubjectResponse> getById(@PathVariable String id) {
		return getSubjectsUseCase.getById(id)
				.map(SubjectResponse::from)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	record SubjectResponse(String id, String name, String description, List<ConceptResponse> concepts) {

		static SubjectResponse from(Subject subject) {
			return new SubjectResponse(subject.id(), subject.name(), subject.description(),
					subject.concepts().stream().map(ConceptResponse::from).toList());
		}
	}

	record ConceptResponse(String id, String name, String description, int difficulty) {

		static ConceptResponse from(Concept concept) {
			return new ConceptResponse(concept.id(), concept.name(), concept.description(), concept.difficulty());
		}
	}
}
