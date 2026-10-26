package com.cognia.api.application.service;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.application.port.in.LearningPathUseCases;
import com.cognia.api.application.port.out.*;
import com.cognia.api.domain.model.*;
import com.cognia.api.domain.service.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class ReviewAndGlobalExamServiceTest {
	private static final UUID student=UUID.randomUUID(), subject=UUID.randomUUID(), concept=UUID.randomUUID();
	private static final Instant NOW=Instant.parse("2026-04-05T10:15:30Z");
	private Fixture fixture(){return new Fixture();}

	@Test void gradeUsesInjectedClockAndClosingKeepsConsistentTotals(){var f=fixture();var session=f.service.startReview(student,subject);var receipt=f.service.grade(student,session.id(),f.p1.id(),3,null);assertEquals(NOW.plus(Duration.ofDays(1)),receipt.nextReviewAt());var closed=f.service.finishReview(student,session.id());assertEquals(1,closed.reviewed());assertEquals(1,closed.remembered());assertEquals(0,closed.forgotten());}
	@Test void rejectsOutOfRangeGradesAndDuplicateCardInOneSession(){var f=fixture();var session=f.service.startReview(student,subject);assertThrows(InvalidPracticeRequestException.class,()->f.service.grade(student,session.id(),f.p1.id(),7,null));f.service.grade(student,session.id(),f.p1.id(),4,null);assertThrows(DiagnosticConflictException.class,()->f.service.grade(student,session.id(),f.p1.id(),3,null));}
	@Test void thresholdReopensAConceptOnlyOnceAndResetsEveryCardSequence(){var f=fixture();f.reviews.progresses.put(f.p1.id(),f.progress(f.p1.id(),1,1));f.reviews.progresses.put(f.p2.id(),f.progress(f.p2.id(),1,1));var session=f.service.startReview(student,subject);var first=f.service.grade(student,session.id(),f.p1.id(),1,null);assertTrue(first.recalculationTriggered());assertEquals(1,f.paths.reopenCalls);assertEquals(1,f.deck.resetCalls);assertEquals(0,f.reviews.progresses.get(f.p1.id()).consecutiveLapses());assertEquals(0,f.reviews.progresses.get(f.p2.id()).consecutiveLapses());var second=f.service.grade(student,session.id(),f.p2.id(),0,null);assertFalse(second.recalculationTriggered());assertEquals(1,f.paths.reopenCalls);}
	@Test void refusesExamWhenLatestRouteIsNoLongerCompleted(){var f=fixture();f.exams.currentPathCompleted=false;assertThrows(DiagnosticConflictException.class,()->f.service.startGlobalExam(student,subject));}
	@Test void finishingGlobalExamWithoutAnswersIsAValidationError(){var f=fixture();var exam=f.service.startGlobalExam(student,subject).exam();assertThrows(InvalidPracticeRequestException.class,()->f.service.finishGlobalExam(student,exam.id()));}
	@Test void rejectsDuplicateGlobalExamAnswer(){var f=fixture();var exam=f.service.startGlobalExam(student,subject).exam();f.service.answerGlobalExam(student,exam.id(),f.examActivity.id(),null,"x",null);assertThrows(DiagnosticConflictException.class,()->f.service.answerGlobalExam(student,exam.id(),f.examActivity.id(),null,"x",null));}
	@Test void globalExamFinishesWithScoreAndConceptSummaryWithoutChangingPath(){var f=fixture();var exam=f.service.startGlobalExam(student,subject).exam();f.service.answerGlobalExam(student,exam.id(),f.examActivity.id(),null,"x",null);var result=f.service.finishGlobalExam(student,exam.id());assertEquals(100,result.exam().score());assertTrue(result.exam().passed());assertEquals(List.of(new GlobalExamSummary(concept,1,1)),result.concepts());assertEquals(0,f.paths.reopenCalls);}

	private final class Fixture {
		final ReviewRepo reviews=new ReviewRepo(); final Deck deck=new Deck(); final Exams exams=new Exams(); final Paths paths=new Paths(); final Activity examActivity=new Activity(UUID.randomUUID(),concept,"C1-X","Answer x",null,2,Activity.Usage.EXAMEN_GLOBAL,Activity.Type.RESPUESTA_CORTA,true,List.of(),List.of(new ShortAnswerCriterion("x",false)));
		final ReviewProgress p1=progress(UUID.randomUUID(),0,0),p2=progress(UUID.randomUUID(),0,0);
		final ReviewAndGlobalExamService service=new ReviewAndGlobalExamService(reviews,deck,exams,new Activities(exams),new Concepts(),new Diagnostics(),id->id.equals(student),new Subjects(),paths,new ActivityCorrectionService(),new RuleBasedMasteryPolicy(Map.of(1,1d,2,1.5d,3,2d)),Clock.fixed(NOW,ZoneOffset.UTC),10,2,2,70);
		Fixture(){deck.reviewsRef=reviews;reviews.progresses.put(p1.id(),p1);reviews.progresses.put(p2.id(),p2);exams.items=List.of(examActivity);}
		ReviewProgress progress(UUID id,int all,int consecutive){return new ReviewProgress(id,student,UUID.randomUUID(),concept,2.5,0,0,all,consecutive,NOW.minusSeconds(86400),NOW.minusSeconds(1),ReviewProgress.State.APRENDIENDO);}
	}
	private class ReviewRepo implements ReviewRepository {
		final Map<UUID,ReviewProgress> progresses=new LinkedHashMap<>();final List<ReviewAnswer> answers=new ArrayList<>();ReviewSession session;
		public List<ReviewProgress> due(UUID s,UUID sub,Instant now,int limit){return progresses.values().stream().filter(p->!p.nextReviewAt().isAfter(now)).limit(limit).toList();}
		public Optional<ReviewProgress> progress(UUID s,UUID sub,UUID id){return Optional.ofNullable(progresses.get(id));}
		public ReviewSession findOrStartSession(UUID s,UUID sub,Instant now){if(session==null||!session.inProgress())session=new ReviewSession(UUID.randomUUID(),s,sub,now,null,0,0,0);return session;}
		public Optional<ReviewSession> session(UUID s,UUID id){return session!=null&&session.id().equals(id)?Optional.of(session):Optional.empty();}
		public List<ReviewAnswer> reviewAnswers(UUID id){return List.copyOf(answers);}
		public ReviewAnswer saveGrade(UUID id,ReviewProgress next,int grade,int old){progresses.put(next.id(),next);var a=new ReviewAnswer(UUID.randomUUID(),id,next.id(),grade,old,next.intervalDays(),next.easeFactor());answers.add(a);return a;}
		public ReviewSession finish(ReviewSession s,Instant at){var remembered=(int)answers.stream().filter(a->a.grade()>=3).count();session=new ReviewSession(s.id(),s.studentId(),s.subjectId(),s.startedAt(),at,answers.size(),remembered,answers.size()-remembered);return session;}
	}
	private class Deck implements ReviewDeckPort {
		int resetCalls;public List<ReviewCard> activeCards(UUID c){return List.of();}public Optional<ReviewCard> card(UUID id){return Optional.empty();}public void addMissingProgress(UUID s,UUID c,List<ReviewCard> cards,Instant due){}
		public void resetConsecutiveLapses(UUID s,UUID c){resetCalls++;for(var e:new ArrayList<>(((ReviewRepo)reviewsRef).progresses.entrySet()))if(e.getValue().conceptId().equals(c))((ReviewRepo)reviewsRef).progresses.put(e.getKey(),e.getValue().resetConsecutiveLapses());}
		ReviewRepository reviewsRef;
	}
	private class Exams implements GlobalExamRepository {
		boolean currentPathCompleted=true;List<Activity> items=List.of();final List<EvaluationAnswer> answers=new ArrayList<>();final List<GlobalExam> all=new ArrayList<>();
		public boolean hasCurrentCompletedPath(UUID s,UUID sub){return currentPathCompleted;}public Optional<GlobalExam> inProgress(UUID s,UUID sub){return all.stream().filter(e->e.state().equals("EN_PROGRESO")).findFirst();}public List<GlobalExam> exams(UUID s,UUID sub){return List.copyOf(all);}public Optional<GlobalExam> find(UUID s,UUID id){return all.stream().filter(e->e.id().equals(id)).findFirst();}public GlobalExam save(GlobalExam e){var saved=e.id()==null?new GlobalExam(UUID.randomUUID(),e.studentId(),e.subjectId(),e.attempt(),e.state(),e.finishedAt(),e.score(),e.passed()):e;all.removeIf(old->old.id().equals(saved.id()));all.add(saved);return saved;}public List<EvaluationAnswer> answers(UUID id){return List.copyOf(answers);}public EvaluationAnswer saveAnswer(UUID id,UUID act,UUID option,String text,boolean correct,Integer sec){var a=new EvaluationAnswer(UUID.randomUUID(),id,act,correct);answers.add(a);return a;}
	}
	private class Activities implements ActivityRepository {final Exams exams;Activities(Exams exams){this.exams=exams;}public List<Activity> findPracticeByConcept(UUID c,Integer d){return List.of();}public List<Activity> findDiagnosticBySubject(UUID s){return List.of();}public List<Activity> findGlobalExamBySubject(UUID s){return new ArrayList<>(exams.items);}public Optional<Activity> findById(UUID id){return exams.items.stream().filter(a->a.id().equals(id)).findFirst();}}
	private class Concepts implements ConceptRepository {public List<Concept> findBySubjectId(UUID s){return List.of(new Concept(concept,s,"C1","Concept",null,1,1,true));}public Optional<Concept> findById(UUID id){return Optional.empty();}}
	private class Subjects implements SubjectRepository {public List<Subject> findAll(){return List.of();}public Optional<Subject> findById(UUID id){return Optional.of(new Subject(id,"S","Subject",null,true));}}
	private class Diagnostics implements DiagnosticRepository {final UUID profile=UUID.randomUUID();public Optional<DiagnosticSession> findInProgress(UUID s,UUID sub){return Optional.empty();}public boolean hasCompleted(UUID s,UUID sub){return true;}public DiagnosticSession saveSession(DiagnosticSession s){return s;}public Optional<DiagnosticSession> findSession(UUID id){return Optional.empty();}public List<DiagnosticAnswer> answers(UUID id){return List.of();}public DiagnosticAnswer saveAnswer(DiagnosticAnswer a){return a;}public UUID ensureProfile(UUID s,UUID sub){return profile;}public void completeProfile(UUID id){}public KnowledgeProfile profile(UUID s,UUID sub){return new KnowledgeProfile(profile,s,sub,true,List.of());}public Optional<ConceptMastery> mastery(UUID p,UUID c){return Optional.empty();}public ConceptMastery saveMastery(UUID p,ConceptMastery m){return m;}public void saveHistory(UUID p,UUID c,double old,double next,MasteryChangeSource src,UUID session){} }
	private class Paths implements LearningPathUseCases {int reopenCalls;public boolean reopenAfterReview(UUID s,UUID sub,UUID c){reopenCalls++;return true;}public LearningPath generate(UUID s,UUID sub){throw new UnsupportedOperationException();}public LearningPath get(UUID s,UUID sub){throw new UnsupportedOperationException();}public Recommendation next(UUID s,UUID sub){throw new UnsupportedOperationException();}public EvaluationStart start(UUID s,UUID sub,UUID step){throw new UnsupportedOperationException();}public EvaluationAnswer answer(UUID s,UUID session,UUID activity,UUID option,String text,Integer seconds){throw new UnsupportedOperationException();}public EvaluationResult finish(UUID s,UUID session){throw new UnsupportedOperationException();}public LearningPath recalculate(UUID s,UUID sub){throw new UnsupportedOperationException();}}
}
