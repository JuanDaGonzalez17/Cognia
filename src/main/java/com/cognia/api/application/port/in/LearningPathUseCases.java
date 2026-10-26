package com.cognia.api.application.port.in;
import com.cognia.api.domain.model.*; import java.util.*;
public interface LearningPathUseCases {
	LearningPath generate(UUID student,UUID subject); LearningPath get(UUID student,UUID subject); Recommendation next(UUID student,UUID subject);
	EvaluationStart start(UUID student,UUID subject,UUID stepId); EvaluationAnswer answer(UUID student,UUID session,UUID activity,UUID option,String text,Integer seconds); EvaluationResult finish(UUID student,UUID session); LearningPath recalculate(UUID student,UUID subject);
	boolean reopenAfterReview(UUID student, UUID subject, UUID conceptId);
	record RecommendedActivity(Activity activity,String reason){} record Recommendation(PathStep step,List<RecommendedActivity> activities){}
	record EvaluationStart(ShortEvaluation session,List<Activity> activities){}
	record EvaluationResult(ShortEvaluation session,PathStep.State stepState,boolean recalculated){}
}
