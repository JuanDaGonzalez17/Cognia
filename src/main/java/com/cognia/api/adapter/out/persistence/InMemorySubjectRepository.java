package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Concept;
import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemorySubjectRepository implements SubjectRepository {

	private final Map<String, Subject> subjects = Map.of(
			"integral-calculus",
			new Subject(
					"integral-calculus",
					"Integral Calculus",
					"Study of integration and its applications.",
					List.of(
							new Concept("antiderivatives", "Primitive functions and antiderivatives",
									"Recognize antiderivatives and basic integration rules.", 1),
							new Concept("integration-rules", "Integration rules",
									"Apply linearity, power, and substitution rules.", 2),
							new Concept("definite-integrals", "Definite integrals",
									"Evaluate definite integrals and interpret their meaning.", 2))));

	@Override
	public List<Subject> findAll() {
		return List.copyOf(subjects.values());
	}

	@Override
	public Optional<Subject> findById(String id) {
		return Optional.ofNullable(subjects.get(id));
	}
}
