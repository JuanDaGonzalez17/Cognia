package com.cognia.api.application.service;

import com.cognia.api.application.port.in.SubjectActivityQueries;
import com.cognia.api.application.port.out.SubjectActivityQueriesPort;
import com.cognia.api.application.port.out.SubjectRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectActivityQueriesService implements SubjectActivityQueries {
	private final SubjectActivityQueriesPort queries;
	private final SubjectRepository subjects;
	private final Clock clock;
	private final int historyLimit;
	private final int upcomingLimit;

	public SubjectActivityQueriesService(SubjectActivityQueriesPort queries, SubjectRepository subjects, Clock clock,
			@Value("${activity-history.limit:50}") int historyLimit,
			@Value("${review.upcoming-limit:100}") int upcomingLimit) {
		if (historyLimit < 1 || upcomingLimit < 1) throw new IllegalArgumentException("Query limits must be positive");
		this.queries = queries;
		this.subjects = subjects;
		this.clock = clock;
		this.historyLimit = historyLimit;
		this.upcomingLimit = upcomingLimit;
	}

	@Override @Transactional(readOnly = true)
	public List<Activity> history(UUID studentId, UUID subjectId) {
		checkSubject(subjectId);
		return queries.history(studentId, subjectId, historyLimit);
	}

	@Override @Transactional(readOnly = true)
	public List<UpcomingReview> upcoming(UUID studentId, UUID subjectId) {
		checkSubject(subjectId);
		return queries.upcoming(studentId, subjectId, clock.instant(), upcomingLimit);
	}

	private void checkSubject(UUID id) {
		if (subjects.findById(id).isEmpty()) throw new ResourceNotFoundException("Subject was not found");
	}
}
