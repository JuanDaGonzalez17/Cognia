package com.cognia.api.domain.service;

import com.cognia.api.domain.model.ConceptMastery;
import com.cognia.api.domain.model.DiagnosticEvidence;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RuleBasedMasteryPolicy implements MasteryPolicy {
	private final Map<Integer, Double> weights;
	public RuleBasedMasteryPolicy(Map<Integer, Double> weights) {
		this.weights = Map.copyOf(weights);
		for (int d = 1; d <= 3; d++) if (this.weights.get(d) == null || !Double.isFinite(this.weights.get(d)) || this.weights.get(d) <= 0) throw new IllegalArgumentException("Positive weight required for difficulty " + d);
	}
	@Override public ConceptMastery estimate(UUID conceptId, List<DiagnosticEvidence> evidence) {
		if (evidence.isEmpty()) throw new IllegalArgumentException("Cannot estimate mastery without evidence");
		double total = 0, correct = 0; int count = 0, hits = 0;
		for (var item : evidence) { var weight = weights.get(item.difficulty()); total += weight; if (item.correct()) { correct += weight; hits++; } count++; }
		return new ConceptMastery(conceptId, correct / total, count, hits);
	}
	@Override public ConceptMastery incorporate(UUID conceptId, ConceptMastery previous, List<DiagnosticEvidence> evidence) {
		if (evidence.isEmpty()) throw new IllegalArgumentException("Cannot update mastery without evidence");
		var weight=0d; var correct=0d; var hits=0;
		for(var item:evidence){var w=weights.get(item.difficulty());weight+=w;if(item.correct()){correct+=w;hits++;}}
		var n=previous==null?0:previous.totalEvaluations();var old=previous==null?0d:previous.level();
		var next=(old*n+correct)/(n+weight);
		return new ConceptMastery(conceptId,next,n+evidence.size(),(previous==null?0:previous.totalCorrect())+hits);
	}
}
