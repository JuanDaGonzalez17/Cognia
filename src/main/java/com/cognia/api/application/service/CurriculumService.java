package com.cognia.api.application.service;

import com.cognia.api.application.port.in.AddPrerequisiteUseCase;
import com.cognia.api.application.port.in.GetConceptPrerequisitesUseCase;
import com.cognia.api.application.port.in.GetSubjectConceptsUseCase;
import com.cognia.api.application.port.in.GetSubjectsUseCase;
import com.cognia.api.application.port.out.ConceptRepository;
import com.cognia.api.application.port.out.PrerequisiteRepository;
import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Concept;
import com.cognia.api.domain.model.Prerequisite;
import com.cognia.api.domain.model.Subject;
import com.cognia.api.domain.service.PrerequisiteGraphValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CurriculumService implements GetSubjectsUseCase, GetSubjectConceptsUseCase,
		GetConceptPrerequisitesUseCase, AddPrerequisiteUseCase {
	private final SubjectRepository subjectRepository;
	private final ConceptRepository conceptRepository;
	private final PrerequisiteRepository prerequisiteRepository;
	private final PrerequisiteGraphValidator graphValidator;

	public CurriculumService(SubjectRepository subjectRepository, ConceptRepository conceptRepository,
			PrerequisiteRepository prerequisiteRepository, PrerequisiteGraphValidator graphValidator) {
		this.subjectRepository = subjectRepository;
		this.conceptRepository = conceptRepository;
		this.prerequisiteRepository = prerequisiteRepository;
		this.graphValidator = graphValidator;
	}

	@Override
	public List<Subject> getAll() {
		return subjectRepository.findAll();
	}

	@Override
	public Optional<Subject> getById(UUID id) {
		return subjectRepository.findById(id);
	}

	@Override
	public List<Concept> getConcepts(UUID subjectId) {
		if (subjectRepository.findById(subjectId).isEmpty()) {
			throw new ResourceNotFoundException("Subject " + subjectId + " was not found");
		}
		return conceptRepository.findBySubjectId(subjectId);
	}

	@Override
	public List<Prerequisite> getPrerequisites(UUID conceptId) {
		if (conceptRepository.findById(conceptId).isEmpty()) {
			throw new ResourceNotFoundException("Concept " + conceptId + " was not found");
		}
		return prerequisiteRepository.findByConceptId(conceptId);
	}

	@Override
	public Prerequisite addPrerequisite(UUID conceptId, UUID prerequisiteId) {
		var concept = findConcept(conceptId);
		var prerequisite = findConcept(prerequisiteId);
		var subjectGraph = prerequisiteRepository.findBySubjectId(concept.subjectId());
		graphValidator.validate(concept, prerequisite, subjectGraph);
		return prerequisiteRepository.save(new Prerequisite(concept.subjectId(), conceptId, prerequisiteId));
	}

	private Concept findConcept(UUID id) {
		return conceptRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Concept " + id + " was not found"));
	}
}
