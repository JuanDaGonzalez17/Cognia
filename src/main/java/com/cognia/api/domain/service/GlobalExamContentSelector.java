package com.cognia.api.domain.service;

import com.cognia.api.domain.model.Activity;
import java.util.*;

public class GlobalExamContentSelector {
	public List<Activity> select(List<Activity> activities, Map<UUID, Integer> conceptOrder, int maximumPerConcept) {
		if (maximumPerConcept < 1) throw new IllegalArgumentException("Maximum activities per concept must be positive");
		var sorted = activities.stream().filter(Activity::active).filter(a -> a.usage() == Activity.Usage.EXAMEN_GLOBAL)
			.sorted(Comparator.comparingInt((Activity a) -> conceptOrder.getOrDefault(a.conceptId(), Integer.MAX_VALUE)).thenComparing(Activity::code)).toList();
		var counts = new HashMap<UUID, Integer>();
		return sorted.stream().filter(a -> counts.merge(a.conceptId(), 1, Integer::sum) <= maximumPerConcept).toList();
	}
}
