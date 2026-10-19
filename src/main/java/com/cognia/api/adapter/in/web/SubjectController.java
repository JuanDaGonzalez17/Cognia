package com.cognia.api.adapter.in.web;

import com.cognia.api.application.port.in.GetSubjectsUseCase;
import com.cognia.api.application.service.ResourceNotFoundException;
import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.UUID;
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
	public ResponseEntity<SubjectResponse> getById(@PathVariable UUID id) {
		return getSubjectsUseCase.getById(id)
				.map(SubjectResponse::from)
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new ResourceNotFoundException("Subject " + id + " was not found"));
	}

	public record SubjectResponse(UUID id, String code, String name, String description, boolean active) {
		static SubjectResponse from(Subject subject) {
			return new SubjectResponse(subject.id(), subject.code(), subject.name(), subject.description(), subject.active());
		}
	}
}
