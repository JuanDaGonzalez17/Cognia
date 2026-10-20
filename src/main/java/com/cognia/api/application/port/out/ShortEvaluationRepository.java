package com.cognia.api.application.port.out;
import com.cognia.api.domain.model.*; import java.util.*;
public interface ShortEvaluationRepository {
	Optional<ShortEvaluation> inProgress(UUID student,UUID stepId); Optional<ShortEvaluation> find(UUID sessionId); int nextAttempt(UUID student,UUID conceptId);
	int consecutiveFailures(UUID student,UUID pathStepId);
	int reinforcementDifficulty(UUID student,UUID pathStepId);
	ShortEvaluation save(ShortEvaluation session); List<EvaluationAnswer> answers(UUID sessionId); EvaluationAnswer saveAnswer(UUID sessionId,UUID activityId,UUID optionId,String answer,boolean correct,Integer seconds);
}
