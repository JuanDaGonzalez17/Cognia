package com.cognia.api.application.service;

import com.cognia.api.application.port.in.DiagnosticUseCases;
import com.cognia.api.application.port.out.*;
import com.cognia.api.domain.model.*;
import com.cognia.api.domain.service.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagnosticService implements DiagnosticUseCases {
	private final DiagnosticRepository repo;
	private final StudentExistencePort students;
	private final SubjectRepository subjects;
	private final ActivityRepository activities;
	private final ActivityCorrectionService correction;
	private final MasteryPolicy policy;

	public DiagnosticService(DiagnosticRepository repo, StudentExistencePort students, SubjectRepository subjects,
			ActivityRepository activities, ActivityCorrectionService correction, MasteryPolicy policy) {
		this.repo = repo;
		this.students = students;
		this.subjects = subjects;
		this.activities = activities;
		this.correction = correction;
		this.policy = policy;
	}

	public DiagnosticStart start(UUID studentId, UUID subjectId) {
		ensureStudent(studentId);
		if (subjects.findById(subjectId).isEmpty()) throw new ResourceNotFoundException("Subject " + subjectId + " was not found");
		var existing = repo.findInProgress(studentId, subjectId);
		if (existing.isPresent()) return new DiagnosticStart(existing.get(), diagnosticActivities(subjectId));
		if (repo.hasCompleted(studentId, subjectId)) throw new DiagnosticConflictException("A diagnostic has already been completed for this subject");
		repo.ensureProfile(studentId, subjectId);
		var session = repo.saveSession(new DiagnosticSession(null, studentId, subjectId, "EN_PROGRESO", null, null));
		return new DiagnosticStart(session, diagnosticActivities(subjectId));
	}

	private List<Activity> diagnosticActivities(UUID subject) {
		return activities.findDiagnosticBySubject(subject);
	}

	public DiagnosticAnswer answer(UUID student, UUID sessionId, UUID activityId, UUID optionId, String answer, Integer seconds) {
		ensureStudent(student);
		var session = ownedSession(student, sessionId);
		if (!session.state().equals("EN_PROGRESO")) throw new DiagnosticConflictException("Diagnostic session is not in progress");
		if (repo.answers(sessionId).stream().anyMatch(a -> a.activityId().equals(activityId))) throw new DiagnosticConflictException("Activity was already answered in this session");
		if (seconds != null && seconds < 0) throw new InvalidPracticeRequestException("Seconds must not be negative");
		var activity = activities.findById(activityId).orElseThrow(() -> new ResourceNotFoundException("Activity " + activityId + " was not found"));
		if (activity.usage() != Activity.Usage.DIAGNOSTICO || !activity.active()
				|| !activities.findDiagnosticBySubject(session.subjectId()).stream().anyMatch(item -> item.id().equals(activity.id()))) {
			throw new InvalidPracticeRequestException("Activity is not an active diagnostic activity for this subject");
		}

		boolean skipped = optionId == null && (answer == null || answer.isBlank());
		String recordedAnswer = skipped ? null : answer;
		boolean correctAnswer;
		try {
			if (skipped) {
				correctAnswer = false;
			} else if (activity.type() == Activity.Type.SELECCION_MULTIPLE) {
				if (optionId == null || answer != null) throw new IllegalArgumentException("Select exactly one option");
				correctAnswer = correction.correctChoice(activity, optionId);
			} else {
				if (optionId != null) throw new IllegalArgumentException("Short answer cannot include an option");
				correction.validateAnswer(answer);
				correctAnswer = correction.correctShortAnswer(activity, answer);
			}
		} catch (IllegalArgumentException ex) {
			throw new InvalidPracticeRequestException(ex.getMessage());
		}
		return repo.saveAnswer(new DiagnosticAnswer(null, sessionId, activityId, optionId, recordedAnswer, correctAnswer, seconds));
	}

	@Transactional
	public DiagnosticSession finish(UUID student, UUID sessionId) {
		ensureStudent(student);
		var session = ownedSession(student, sessionId);
		if (!session.state().equals("EN_PROGRESO")) throw new DiagnosticConflictException("Diagnostic session is not in progress");
		var answers = repo.answers(sessionId);
		if (answers.isEmpty()) throw new InvalidPracticeRequestException("Cannot finish a diagnostic without answers");
		var profileId = repo.ensureProfile(student, session.subjectId());
		Map<UUID, List<DiagnosticEvidence>> byConcept = new HashMap<>();
		for (var response : answers) {
			var activity = activities.findById(response.activityId()).orElseThrow();
			byConcept.computeIfAbsent(activity.conceptId(), key -> new ArrayList<>()).add(new DiagnosticEvidence(activity.difficulty(), response.correct()));
		}
		for (var entry : byConcept.entrySet()) {
			var mastery = policy.estimate(entry.getKey(), entry.getValue());
			var previous = repo.mastery(profileId, entry.getKey());
			double old = previous.map(ConceptMastery::level).orElse(0d);
			repo.saveMastery(profileId, mastery);
			repo.saveHistory(profileId, entry.getKey(), old, mastery.level(), MasteryChangeSource.DIAGNOSTICO, session.id());
		}
		double score = 100d * answers.stream().filter(DiagnosticAnswer::correct).count() / answers.size();
		var complete = repo.saveSession(new DiagnosticSession(session.id(), student, session.subjectId(), "COMPLETADA", score, Instant.now()));
		repo.completeProfile(profileId);
		return complete;
	}

	@Transactional
	public DiagnosticSession abandon(UUID student, UUID sessionId) {
		ensureStudent(student);
		var session = ownedSession(student, sessionId);
		if (!session.state().equals("EN_PROGRESO")) throw new DiagnosticConflictException("Diagnostic session is not in progress");
		return repo.saveSession(new DiagnosticSession(session.id(), student, session.subjectId(), "ABANDONADA", null, Instant.now()));
	}

	public KnowledgeProfile profile(UUID student, UUID subject) {
		ensureStudent(student);
		if (subjects.findById(subject).isEmpty()) throw new ResourceNotFoundException("Subject " + subject + " was not found");
		return repo.profile(student, subject);
	}

	private DiagnosticSession ownedSession(UUID student, UUID id) {
		var session = repo.findSession(id).orElseThrow(() -> new ResourceNotFoundException("Diagnostic session was not found"));
		if (!session.studentId().equals(student)) throw new DiagnosticConflictException("Diagnostic session belongs to another student");
		return session;
	}

	private void ensureStudent(UUID id) {
		if (!students.exists(id)) throw new ResourceNotFoundException("Student " + id + " was not found");
	}
}
