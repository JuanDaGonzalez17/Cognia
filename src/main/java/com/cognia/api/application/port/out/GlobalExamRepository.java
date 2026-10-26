package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.*;
import java.util.*;

public interface GlobalExamRepository {
	boolean hasCurrentCompletedPath(UUID studentId, UUID subjectId);
	Optional<GlobalExam> inProgress(UUID studentId, UUID subjectId);
	List<GlobalExam> exams(UUID studentId, UUID subjectId);
	Optional<GlobalExam> find(UUID studentId, UUID examId);
	GlobalExam save(GlobalExam exam);
	List<EvaluationAnswer> answers(UUID examId);
	EvaluationAnswer saveAnswer(UUID examId, UUID activityId, UUID optionId, String text, boolean correct, Integer seconds);
}
