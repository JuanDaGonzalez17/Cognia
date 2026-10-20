package com.cognia.api.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.domain.model.DiagnosticEvidence;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RuleBasedMasteryPolicyTest {
	private final RuleBasedMasteryPolicy policy = new RuleBasedMasteryPolicy(Map.of(1, 1.0, 2, 1.5, 3, 2.0));
	private final UUID concept = UUID.randomUUID();
	private record Case(List<DiagnosticEvidence> evidence,double expected) {}
	@Test void estimatesTabulatedBoundaryAndSingleItemCases() {
		var cases = List.of(
			new Case(List.of(new DiagnosticEvidence(1,true),new DiagnosticEvidence(2,true)),1.0),
			new Case(List.of(new DiagnosticEvidence(1,false),new DiagnosticEvidence(3,false)),0.0),
			new Case(List.of(new DiagnosticEvidence(2,true)),1.0),
			new Case(List.of(new DiagnosticEvidence(2,false)),0.0),
			new Case(List.of(new DiagnosticEvidence(1,true),new DiagnosticEvidence(3,false)),1.0/3.0));
		for (var c : cases) assertEquals(c.expected(), policy.estimate(concept,c.evidence()).level(), 0.00001);
	}
	@Test void sameInputAlwaysProducesSameResultAndLevelStaysInRange() {
		var evidence = List.of(new DiagnosticEvidence(1,true),new DiagnosticEvidence(3,false),new DiagnosticEvidence(2,true));
		var first=policy.estimate(concept,evidence); var second=policy.estimate(concept,evidence);
		assertEquals(first,second); assertTrue(first.level()>=0 && first.level()<=1);
	}
}
