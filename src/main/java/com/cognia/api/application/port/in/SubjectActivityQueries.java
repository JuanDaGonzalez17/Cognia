package com.cognia.api.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SubjectActivityQueries {
	List<Activity> history(UUID studentId, UUID subjectId);
	List<UpcomingReview> upcoming(UUID studentId, UUID subjectId);

	record Activity(UUID id, String type, UUID conceptId, String conceptName, Double score,
		Boolean passed, Instant date, Integer attempt) { }
	record UpcomingReview(UUID progressId, UUID conceptId, String conceptName, Instant nextReviewAt,
		int intervalDays, int consecutiveLapses) { }
}
