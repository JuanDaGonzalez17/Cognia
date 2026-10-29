package com.cognia.api.application.service;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.application.port.in.SubjectActivityQueries.Activity;
import com.cognia.api.application.port.in.SubjectActivityQueries.UpcomingReview;
import com.cognia.api.application.port.out.SubjectActivityQueriesPort;
import com.cognia.api.application.port.out.SubjectRepository;
import com.cognia.api.domain.model.Subject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.jupiter.api.Test;

class SubjectActivityQueriesServiceTest {
	private static final UUID SUBJECT = UUID.randomUUID();
	private static final UUID STUDENT = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-10-28T12:00:00Z");

	@Test void historyPassesConfiguredLimitAndPreservesMappedTypesAndOrder() {
		var newest = new Activity(UUID.randomUUID(), "REPASO", null, "Repaso", null, null, NOW, null);
		var older = new Activity(UUID.randomUUID(), "DIAGNOSTICO", null, "Diagnóstico inicial", 80d, null, NOW.minusSeconds(2), 1);
		var output = new ArrayList<Activity>();
		var port = new FakePort(output, List.of(newest, older), List.of());
		var service = service(port);
		assertEquals(List.of(newest, older), service.history(STUDENT, SUBJECT));
		assertEquals(50, port.historyLimit);
	}

	@Test void upcomingUsesClockAndConfiguredLimitAndReturnsAscendingFutureItems() {
		var soon = new UpcomingReview(UUID.randomUUID(), UUID.randomUUID(), "Álgebra", NOW.plusSeconds(60), 2, 0);
		var later = new UpcomingReview(UUID.randomUUID(), UUID.randomUUID(), "Cálculo", NOW.plusSeconds(600), 5, 1);
		var port = new FakePort(new ArrayList<>(), List.of(), List.of(soon, later));
		var results = service(port).upcoming(STUDENT, SUBJECT);
		assertEquals(List.of(soon, later), results);
		assertEquals(NOW, port.now);
		assertEquals(100, port.upcomingLimit);
	}

	@Test void missingSubjectRaisesNotFound() {
		var service = new SubjectActivityQueriesService(new FakePort(new ArrayList<>(), List.of(), List.of()),
			new SubjectRepository() { public List<Subject> findAll() { return List.of(); } public Optional<Subject> findById(UUID id) { return Optional.empty(); } },
			Clock.fixed(NOW, ZoneOffset.UTC), 50, 100);
		assertThrows(ResourceNotFoundException.class, () -> service.history(STUDENT, SUBJECT));
	}

	private SubjectActivityQueriesService service(FakePort port) {
		var subjects = new SubjectRepository() { public List<Subject> findAll() { return List.of(); } public Optional<Subject> findById(UUID id) { return Optional.of(new Subject(id, "SUB", "Subject", null, true)); } };
		return new SubjectActivityQueriesService(port, subjects, Clock.fixed(NOW, ZoneOffset.UTC), 50, 100);
	}

	private static class FakePort implements SubjectActivityQueriesPort {
		final List<Activity> activities; final List<UpcomingReview> reviews; int historyLimit; int upcomingLimit; Instant now;
		FakePort(List<Activity> ignored, List<Activity> activities, List<UpcomingReview> reviews) { this.activities = activities; this.reviews = reviews; }
		public List<Activity> history(UUID student, UUID subject, int limit) { historyLimit = limit; return activities; }
		public List<UpcomingReview> upcoming(UUID student, UUID subject, Instant now, int limit) { this.now = now; upcomingLimit = limit; return reviews; }
	}
}
