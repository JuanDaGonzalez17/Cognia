package com.cognia.api.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cognia.api.application.port.out.ConceptRepository;
import com.cognia.api.application.port.out.PrerequisiteRepository;
import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Concept;
import com.cognia.api.domain.model.InvalidPrerequisiteException;
import com.cognia.api.domain.model.Prerequisite;
import com.cognia.api.domain.model.Subject;
import com.cognia.api.domain.service.PrerequisiteGraphValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CurriculumServiceTest {
	private static final UUID SUBJECT_ID = id(100);
	private final Subject subject = new Subject(SUBJECT_ID, "CALC", "Calculus", null, true);
	private final Concept concept = concept(1, SUBJECT_ID);
	private final Concept prerequisite = concept(2, SUBJECT_ID);
	private final FakeSubjectRepository subjects = new FakeSubjectRepository();
	private final FakeConceptRepository concepts = new FakeConceptRepository();
	private final FakePrerequisiteRepository prerequisites = new FakePrerequisiteRepository();
	private final CurriculumService service = new CurriculumService(subjects, concepts, prerequisites,
			new PrerequisiteGraphValidator());

	@Test
	void delegatesSubjectQueriesToOutputPort() {
		assertEquals(List.of(subject), service.getAll());
		assertEquals(Optional.of(subject), service.getById(SUBJECT_ID));
	}

	@Test
	void returnsConceptsForExistingSubject() {
		assertEquals(List.of(concept, prerequisite), service.getConcepts(SUBJECT_ID));
	}

	@Test
	void rejectsConceptLookupWhenSubjectDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> service.getConcepts(id(999)));
	}

	@Test
	void validatesGraphBeforeSavingPrerequisite() {
		var result = service.addPrerequisite(concept.id(), prerequisite.id());

		assertEquals(new Prerequisite(SUBJECT_ID, concept.id(), prerequisite.id()), result);
		assertEquals(List.of(result), prerequisites.saved);
	}

	@Test
	void doesNotSavePrerequisiteFromAnotherSubject() {
		concepts.items.put(prerequisite.id(), concept(2, id(200)));

		assertThrows(InvalidPrerequisiteException.class,
				() -> service.addPrerequisite(concept.id(), prerequisite.id()));
		assertEquals(List.of(), prerequisites.saved);
	}

	private static Concept concept(int id, UUID subjectId) {
		return new Concept(id(id), subjectId, "C" + id, "Concept " + id, null, 1, id, true);
	}

	private static UUID id(int value) {
		return new UUID(0, value);
	}

	private final class FakeSubjectRepository implements SubjectRepository {
		@Override
		public List<Subject> findAll() {
			return List.of(subject);
		}

		@Override
		public Optional<Subject> findById(UUID id) {
			return id.equals(SUBJECT_ID) ? Optional.of(subject) : Optional.empty();
		}
	}

	private final class FakeConceptRepository implements ConceptRepository {
		private final java.util.Map<UUID, Concept> items = new java.util.HashMap<>(
				java.util.Map.of(concept.id(), concept, prerequisite.id(), prerequisite));

		@Override
		public List<Concept> findBySubjectId(UUID subjectId) {
			return items.values().stream().filter(item -> item.subjectId().equals(subjectId))
					.sorted(java.util.Comparator.comparingInt(Concept::order)).toList();
		}

		@Override
		public Optional<Concept> findById(UUID id) {
			return Optional.ofNullable(items.get(id));
		}
	}

	private final class FakePrerequisiteRepository implements PrerequisiteRepository {
		private final List<Prerequisite> saved = new ArrayList<>();

		@Override
		public List<Prerequisite> findByConceptId(UUID conceptId) {
			return saved.stream().filter(edge -> edge.conceptId().equals(conceptId)).toList();
		}

		@Override
		public List<Prerequisite> findBySubjectId(UUID subjectId) {
			return saved.stream().filter(edge -> edge.subjectId().equals(subjectId)).toList();
		}

		@Override
		public Prerequisite save(Prerequisite prerequisite) {
			saved.add(prerequisite);
			return prerequisite;
		}
	}
}
