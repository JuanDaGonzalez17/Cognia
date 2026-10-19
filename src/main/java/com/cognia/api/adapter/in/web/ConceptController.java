package com.cognia.api.adapter.in.web;

import com.cognia.api.application.port.in.GetSubjectConceptsUseCase;
import com.cognia.api.domain.model.Concept;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subjects/{subjectId}/concepts")
public class ConceptController {
	private final GetSubjectConceptsUseCase getSubjectConceptsUseCase;

	public ConceptController(GetSubjectConceptsUseCase getSubjectConceptsUseCase) {
		this.getSubjectConceptsUseCase = getSubjectConceptsUseCase;
	}

	@GetMapping
	public List<ConceptResponse> getBySubject(@PathVariable UUID subjectId) {
		return getSubjectConceptsUseCase.getConcepts(subjectId).stream().map(ConceptResponse::from).toList();
	}

	public record ConceptResponse(UUID id, UUID subjectId, String code, String name, String description,
			int level, int order, boolean active) {
		static ConceptResponse from(Concept concept) {
			return new ConceptResponse(concept.id(), concept.subjectId(), concept.code(), concept.name(),
					concept.description(), concept.level(), concept.order(), concept.active());
		}
	}
}
