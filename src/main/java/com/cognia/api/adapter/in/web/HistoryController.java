package com.cognia.api.adapter.in.web;

import com.cognia.api.adapter.in.security.CurrentStudent;
import com.cognia.api.application.port.in.SubjectActivityQueries;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subjects/{subjectId}")
public class HistoryController {
	private final SubjectActivityQueries queries;

	public HistoryController(SubjectActivityQueries queries) { this.queries = queries; }

	@GetMapping("/history")
	public List<ActivityResponse> history(@PathVariable UUID subjectId, @CurrentStudent UUID student) {
		return queries.history(student, subjectId).stream().map(ActivityResponse::from).toList();
	}

	@GetMapping("/reviews/upcoming")
	public List<UpcomingReviewResponse> upcoming(@PathVariable UUID subjectId, @CurrentStudent UUID student) {
		return queries.upcoming(student, subjectId).stream().map(UpcomingReviewResponse::from).toList();
	}

	public record ActivityResponse(UUID id, String type, UUID conceptId, String conceptName, Double score,
		Boolean passed, Instant date, Integer attempt) {
		static ActivityResponse from(SubjectActivityQueries.Activity a) {
			return new ActivityResponse(a.id(), a.type(), a.conceptId(), a.conceptName(), a.score(), a.passed(), a.date(), a.attempt());
		}
	}
	public record UpcomingReviewResponse(UUID progressId, UUID conceptId, String conceptName, Instant nextReviewAt,
		int intervalDays, int consecutiveLapses) {
		static UpcomingReviewResponse from(SubjectActivityQueries.UpcomingReview r) {
			return new UpcomingReviewResponse(r.progressId(), r.conceptId(), r.conceptName(), r.nextReviewAt(), r.intervalDays(), r.consecutiveLapses());
		}
	}
}
