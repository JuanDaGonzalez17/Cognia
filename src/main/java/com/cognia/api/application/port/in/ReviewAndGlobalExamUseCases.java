package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.*;
import java.time.Instant;
import java.util.*;

public interface ReviewAndGlobalExamUseCases {
	List<DueCard> due(UUID studentId, UUID subjectId);
	ReviewSession startReview(UUID studentId, UUID subjectId);
	ReviewReceipt grade(UUID studentId, UUID sessionId, UUID progressId, int grade, Integer seconds);
	ReviewSession finishReview(UUID studentId, UUID sessionId);
	GlobalExamStart startGlobalExam(UUID studentId, UUID subjectId);
	EvaluationAnswer answerGlobalExam(UUID studentId, UUID examId, UUID activityId, UUID optionId, String text, Integer seconds);
	GlobalExamResult finishGlobalExam(UUID studentId, UUID examId);
	record DueCard(ReviewProgress progress, ReviewCard card) { }
	record ReviewReceipt(UUID progressId, int intervalDays, Instant nextReviewAt, double easeFactor, boolean recalculationTriggered) { }
	record GlobalExamStart(GlobalExam exam, List<Activity> activities) { public GlobalExamStart { activities = List.copyOf(activities); } }
	record GlobalExamResult(GlobalExam exam, List<GlobalExamSummary> concepts) { public GlobalExamResult { concepts = List.copyOf(concepts); } }
}
