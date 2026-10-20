package com.cognia.api.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Activity(UUID id, UUID conceptId, String code, String statement, String feedback,
		int difficulty, Usage usage, Type type, boolean active, List<ActivityOption> options,
		List<ShortAnswerCriterion> criteria) {
	public enum Usage { DIAGNOSTICO, PRACTICA, EVALUACION_CORTA, EXAMEN_GLOBAL }
	public enum Type { SELECCION_MULTIPLE, RESPUESTA_CORTA }
	public Activity {
		Objects.requireNonNull(id); Objects.requireNonNull(conceptId); Objects.requireNonNull(usage); Objects.requireNonNull(type);
		if (code == null || code.isBlank() || statement == null || statement.isBlank()) throw new IllegalArgumentException("Activity code and statement are required");
		if (difficulty < 1 || difficulty > 3) throw new IllegalArgumentException("Difficulty must be between 1 and 3");
		options = List.copyOf(options); criteria = List.copyOf(criteria);
		if (type == Type.SELECCION_MULTIPLE && (options.size() < 2 || options.stream().filter(ActivityOption::correct).count() != 1 || !criteria.isEmpty())) throw new IllegalArgumentException("Multiple choice requires options with exactly one correct answer and no criteria");
		if (type == Type.RESPUESTA_CORTA && (criteria.isEmpty() || !options.isEmpty())) throw new IllegalArgumentException("Short answer requires criteria and no options");
	}
}
