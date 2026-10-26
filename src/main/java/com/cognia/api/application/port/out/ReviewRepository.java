package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.*;
import java.time.Instant;
import java.util.*;

public interface ReviewRepository {
	List<ReviewProgress> due(UUID studentId, UUID subjectId, Instant now, int limit);
	Optional<ReviewProgress> progress(UUID studentId, UUID subjectId, UUID progressId);
	ReviewSession findOrStartSession(UUID studentId, UUID subjectId, Instant now);
	Optional<ReviewSession> session(UUID studentId, UUID sessionId);
	List<ReviewAnswer> reviewAnswers(UUID sessionId);
	ReviewAnswer saveGrade(UUID sessionId, ReviewProgress next, int grade, int oldInterval);
	ReviewSession finish(ReviewSession session, Instant at);
}
