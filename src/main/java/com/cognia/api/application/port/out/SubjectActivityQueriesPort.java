package com.cognia.api.application.port.out;

import com.cognia.api.application.port.in.SubjectActivityQueries.Activity;
import com.cognia.api.application.port.in.SubjectActivityQueries.UpcomingReview;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SubjectActivityQueriesPort {
	List<Activity> history(UUID studentId, UUID subjectId, int limit);
	List<UpcomingReview> upcoming(UUID studentId, UUID subjectId, Instant now, int limit);
}
