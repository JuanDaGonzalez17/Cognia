package com.cognia.api.adapter.in.web;

import com.cognia.api.application.port.in.AddPrerequisiteUseCase;
import com.cognia.api.application.port.in.GetConceptPrerequisitesUseCase;
import com.cognia.api.domain.model.Prerequisite;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/concepts/{conceptId}/prerequisites")
public class PrerequisiteController {
	private final GetConceptPrerequisitesUseCase getConceptPrerequisitesUseCase;
	private final AddPrerequisiteUseCase addPrerequisiteUseCase;

	public PrerequisiteController(GetConceptPrerequisitesUseCase getConceptPrerequisitesUseCase,
			AddPrerequisiteUseCase addPrerequisiteUseCase) {
		this.getConceptPrerequisitesUseCase = getConceptPrerequisitesUseCase;
		this.addPrerequisiteUseCase = addPrerequisiteUseCase;
	}

	@GetMapping
	public List<PrerequisiteResponse> getForConcept(@PathVariable UUID conceptId) {
		return getConceptPrerequisitesUseCase.getPrerequisites(conceptId).stream()
				.map(PrerequisiteResponse::from).toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PrerequisiteResponse add(@PathVariable UUID conceptId, @Valid @RequestBody AddPrerequisiteRequest request) {
		return PrerequisiteResponse.from(addPrerequisiteUseCase.addPrerequisite(conceptId, request.prerequisiteId()));
	}

	public record AddPrerequisiteRequest(@NotNull UUID prerequisiteId) {
	}

	public record PrerequisiteResponse(UUID subjectId, UUID conceptId, UUID prerequisiteId) {
		static PrerequisiteResponse from(Prerequisite prerequisite) {
			return new PrerequisiteResponse(prerequisite.subjectId(), prerequisite.conceptId(), prerequisite.prerequisiteId());
		}
	}
}
