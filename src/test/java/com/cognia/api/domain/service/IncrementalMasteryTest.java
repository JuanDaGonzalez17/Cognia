package com.cognia.api.domain.service;
import static org.junit.jupiter.api.Assertions.*; import com.cognia.api.domain.model.*; import java.util.*; import org.junit.jupiter.api.Test;
class IncrementalMasteryTest {
	@Test void combinesPreviousEstimateWithDifficultyWeightedNewEvidence(){var policy=new RuleBasedMasteryPolicy(Map.of(1,1d,2,1.5,3,2d));var old=new ConceptMastery(UUID.fromString("00000000-0000-0000-0000-000000000001"),.6,5,3);var updated=policy.incorporate(old.conceptId(),old,List.of(new DiagnosticEvidence(3,true),new DiagnosticEvidence(2,true),new DiagnosticEvidence(1,false)));assertEquals(.6315789,updated.level(),.000001);assertEquals(8,updated.totalEvaluations());assertEquals(5,updated.totalCorrect());}
}
