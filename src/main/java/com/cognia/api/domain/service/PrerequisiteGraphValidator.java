package com.cognia.api.domain.service;

import com.cognia.api.domain.model.Concept;
import com.cognia.api.domain.model.InvalidPrerequisiteException;
import com.cognia.api.domain.model.InvalidPrerequisiteException.Reason;
import com.cognia.api.domain.model.Prerequisite;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PrerequisiteGraphValidator {
	public void validate(Concept concept, Concept prerequisite, List<Prerequisite> graph) {
		if (concept.id().equals(prerequisite.id())) {
			throw new InvalidPrerequisiteException(Reason.INVALID_REQUEST,
					"A concept cannot be its own prerequisite");
		}
		if (!concept.subjectId().equals(prerequisite.subjectId())) {
			throw new InvalidPrerequisiteException(Reason.INVALID_REQUEST,
					"A prerequisite must belong to the same subject as the concept");
		}

		var adjacency = new HashMap<UUID, Set<UUID>>();
		for (var edge : graph) {
			adjacency.computeIfAbsent(edge.conceptId(), ignored -> new HashSet<>()).add(edge.prerequisiteId());
		}
		if (adjacency.getOrDefault(concept.id(), Set.of()).contains(prerequisite.id())) {
			throw new InvalidPrerequisiteException(Reason.GRAPH_CONFLICT,
					"This prerequisite relationship already exists");
		}
		if (isReachable(prerequisite.id(), concept.id(), adjacency)) {
			throw new InvalidPrerequisiteException(Reason.GRAPH_CONFLICT,
					"The prerequisite relationship would create a cycle");
		}
	}

	private boolean isReachable(UUID start, UUID target, Map<UUID, Set<UUID>> adjacency) {
		var pending = new ArrayDeque<UUID>();
		var visited = new HashSet<UUID>();
		pending.add(start);
		while (!pending.isEmpty()) {
			var current = pending.removeFirst();
			if (!visited.add(current)) {
				continue;
			}
			if (current.equals(target)) {
				return true;
			}
			pending.addAll(adjacency.getOrDefault(current, Set.of()));
		}
		return false;
	}
}
