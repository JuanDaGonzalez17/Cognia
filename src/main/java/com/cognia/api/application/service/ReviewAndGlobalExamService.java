package com.cognia.api.application.service;

import com.cognia.api.application.port.in.ReviewAndGlobalExamUseCases;
import com.cognia.api.application.port.in.LearningPathUseCases;
import com.cognia.api.application.port.out.*;
import com.cognia.api.domain.model.*;
import com.cognia.api.domain.service.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class ReviewAndGlobalExamService implements ReviewAndGlobalExamUseCases {
	private final ReviewRepository reviews;
	private final ReviewDeckPort deck;
	private final GlobalExamRepository exams;
	private final ActivityRepository activities;
	private final ConceptRepository concepts;
	private final DiagnosticRepository diagnostics;
	private final StudentExistencePort students;
	private final SubjectRepository subjects;
	private final LearningPathUseCases paths;
	private final ActivityCorrectionService correction;
	private final MasteryPolicy mastery;
	private final Sm2Service sm2 = new Sm2Service();
	private final GlobalExamContentSelector selector = new GlobalExamContentSelector();
	private final Clock clock;
	private final int queueLimit, maxPerConcept, lapseThreshold;
	private final double passingThreshold;

	public ReviewAndGlobalExamService(ReviewRepository reviews, ReviewDeckPort deck, GlobalExamRepository exams,
			ActivityRepository activities, ConceptRepository concepts, DiagnosticRepository diagnostics,
			StudentExistencePort students, SubjectRepository subjects, LearningPathUseCases paths,
			ActivityCorrectionService correction, MasteryPolicy mastery, Clock clock,
			@Value("${review.queue-limit:20}") int queueLimit,
			@Value("${review.consecutive-lapses-before-recalculation:2}") int lapseThreshold,
			@Value("${global-exam.max-per-concept:2}") int maxPerConcept,
			@Value("${global-exam.passing-threshold:70}") double passingThreshold) {
		this.reviews=reviews; this.deck=deck; this.exams=exams; this.activities=activities; this.concepts=concepts;
		this.diagnostics=diagnostics; this.students=students; this.subjects=subjects; this.paths=paths;
		this.correction=correction; this.mastery=mastery; this.clock=clock;
		if(queueLimit<1||maxPerConcept<1||lapseThreshold<1||!Double.isFinite(passingThreshold)||passingThreshold<0||passingThreshold>100) throw new IllegalArgumentException("Invalid review or exam configuration");
		this.queueLimit=queueLimit; this.lapseThreshold=lapseThreshold; this.maxPerConcept=maxPerConcept; this.passingThreshold=passingThreshold;
	}

	@Override @Transactional(readOnly=true) public List<DueCard> due(UUID student,UUID subject){checkStudent(student);checkSubject(subject);var now=clock.instant();return reviews.due(student,subject,now,queueLimit).stream().map(p->new DueCard(p,deck.card(p.cardId()).orElseThrow(()->new ResourceNotFoundException("Review card was not found")))).toList();}
	@Override public ReviewSession startReview(UUID student,UUID subject){checkStudent(student);checkSubject(subject);return reviews.findOrStartSession(student,subject,clock.instant());}
	@Override public ReviewReceipt grade(UUID student,UUID sessionId,UUID progressId,int grade,Integer seconds){checkStudent(student);if(grade<0||grade>5)throw new InvalidPracticeRequestException("SM-2 grade must be between 0 and 5");if(progressId==null)throw new InvalidPracticeRequestException("Review progress is required");if(seconds!=null&&seconds<0)throw new InvalidPracticeRequestException("Seconds must not be negative");var session=reviews.session(student,sessionId).orElseThrow(()->new ResourceNotFoundException("Review session was not found"));if(!session.inProgress())throw new DiagnosticConflictException("Review session is already closed");var previousAnswers=reviews.reviewAnswers(sessionId);if(previousAnswers.size()>=queueLimit)throw new DiagnosticConflictException("Review session reached its card limit");if(previousAnswers.stream().anyMatch(a->a.progressId().equals(progressId)))throw new DiagnosticConflictException("Card was already graded in this review session");var before=reviews.progress(student,session.subjectId(),progressId).orElseThrow(()->new ResourceNotFoundException("Review card progress was not found"));var now=clock.instant();if(before.nextReviewAt().isAfter(now))throw new DiagnosticConflictException("Review card is not due yet");var computed=sm2.grade(before,grade);var next=new ReviewProgress(before.id(),before.studentId(),before.cardId(),before.conceptId(),computed.easeFactor(),computed.intervalDays(),computed.repetitions(),computed.totalLapses(),computed.consecutiveLapses(),now,now.plus(Duration.ofDays(computed.intervalDays())),computed.state());reviews.saveGrade(sessionId,next,grade,before.intervalDays());var triggered=false;if(computed.consecutiveLapses()>=lapseThreshold&&before.consecutiveLapses()<lapseThreshold){triggered=paths.reopenAfterReview(student,session.subjectId(),before.conceptId());if(triggered)deck.resetConsecutiveLapses(student,before.conceptId());}return new ReviewReceipt(progressId,next.intervalDays(),next.nextReviewAt(),next.easeFactor(),triggered);}
	@Override public ReviewSession finishReview(UUID student,UUID sessionId){checkStudent(student);var session=reviews.session(student,sessionId).orElseThrow(()->new ResourceNotFoundException("Review session was not found"));if(!session.inProgress())throw new DiagnosticConflictException("Review session is already closed");return reviews.finish(session,clock.instant());}
	@Override public GlobalExamStart startGlobalExam(UUID student,UUID subject){checkStudent(student);checkSubject(subject);requireCurrentCompletedPath(student,subject);var items=examItems(subject);if(items.isEmpty())throw new InvalidPracticeRequestException("No active global-exam activities are available");var existing=exams.inProgress(student,subject);if(existing.isPresent())return new GlobalExamStart(existing.get(),items);var attempt=exams.exams(student,subject).stream().mapToInt(GlobalExam::attempt).max().orElse(0)+1;var session=exams.save(new GlobalExam(null,student,subject,attempt,"EN_PROGRESO",null,null,null));return new GlobalExamStart(session,items);}
	@Override public EvaluationAnswer answerGlobalExam(UUID student,UUID examId,UUID activityId,UUID optionId,String text,Integer seconds){checkStudent(student);var session=ownedExam(student,examId);requireCurrentCompletedPath(student,session.subjectId());if(!"EN_PROGRESO".equals(session.state()))throw new DiagnosticConflictException("Global exam is not in progress");if(exams.answers(examId).stream().anyMatch(a->a.activityId().equals(activityId)))throw new DiagnosticConflictException("Activity was already answered in this exam");if(seconds!=null&&seconds<0)throw new InvalidPracticeRequestException("Seconds must not be negative");var activity=activities.findById(activityId).orElseThrow(()->new ResourceNotFoundException("Activity was not found"));if(!examItems(session.subjectId()).stream().anyMatch(a->a.id().equals(activityId)))throw new InvalidPracticeRequestException("Activity is not part of this global exam");boolean correctAnswer;try{if(activity.type()==Activity.Type.SELECCION_MULTIPLE){if(optionId==null||text!=null)throw new IllegalArgumentException("Select exactly one option");correctAnswer=correction.correctChoice(activity,optionId);}else{if(optionId!=null)throw new IllegalArgumentException("Short answer cannot include an option");correction.validateAnswer(text);correctAnswer=correction.correctShortAnswer(activity,text);}}catch(IllegalArgumentException e){throw new InvalidPracticeRequestException(e.getMessage());}return exams.saveAnswer(examId,activityId,optionId,text,correctAnswer,seconds);}
	@Override public GlobalExamResult finishGlobalExam(UUID student,UUID examId){checkStudent(student);var session=ownedExam(student,examId);requireCurrentCompletedPath(student,session.subjectId());if(!"EN_PROGRESO".equals(session.state()))throw new DiagnosticConflictException("Global exam is not in progress");var answers=exams.answers(examId);if(answers.isEmpty())throw new InvalidPracticeRequestException("Cannot finish a global exam without answers");var selected=examItems(session.subjectId());var activityById=selected.stream().collect(Collectors.toMap(Activity::id,a->a));var score=100d*answers.stream().filter(EvaluationAnswer::correct).count()/answers.size();var passed=score>=passingThreshold;var profileId=diagnostics.ensureProfile(student,session.subjectId());var summaries=new ArrayList<GlobalExamSummary>();var byConcept=answers.stream().collect(Collectors.groupingBy(a->activityById.get(a.activityId()).conceptId(),LinkedHashMap::new,Collectors.toList()));for(var entry:byConcept.entrySet()){var conceptId=entry.getKey();var evidence=entry.getValue().stream().map(a->new DiagnosticEvidence(activityById.get(a.activityId()).difficulty(),a.correct())).toList();var before=diagnostics.mastery(profileId,conceptId).orElse(null);var updated=mastery.incorporate(conceptId,before,evidence);diagnostics.saveMastery(profileId,updated);diagnostics.saveHistory(profileId,conceptId,before==null?0:before.level(),updated.level(),MasteryChangeSource.EXAMEN_GLOBAL,session.id());summaries.add(new GlobalExamSummary(conceptId,(int)entry.getValue().stream().filter(EvaluationAnswer::correct).count(),entry.getValue().size()));}var completed=exams.save(new GlobalExam(session.id(),student,session.subjectId(),session.attempt(),"COMPLETADA",clock.instant(),score,passed));return new GlobalExamResult(completed,summaries);}

	private List<Activity> examItems(UUID subject){var order=new HashMap<UUID,Integer>();concepts.findBySubjectId(subject).forEach(c->order.put(c.id(),c.order()));return selector.select(activities.findGlobalExamBySubject(subject),order,maxPerConcept);}
	private GlobalExam ownedExam(UUID student,UUID id){return exams.find(student,id).orElseThrow(()->new ResourceNotFoundException("Global exam was not found"));}
	private void requireCurrentCompletedPath(UUID student,UUID subject){if(!exams.hasCurrentCompletedPath(student,subject))throw new DiagnosticConflictException("A completed current learning path is required for the global exam");}
	private void checkStudent(UUID id){if(!students.exists(id))throw new ResourceNotFoundException("Student was not found");}
	private void checkSubject(UUID id){if(subjects.findById(id).isEmpty())throw new ResourceNotFoundException("Subject was not found");}
}
