package com.cognia.api.adapter.in.web;
import com.cognia.api.adapter.in.security.CurrentStudent;

import com.cognia.api.application.port.in.ReviewAndGlobalExamUseCases;
import com.cognia.api.domain.model.*;
import java.time.Instant;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1")
public class ReviewAndGlobalExamController {
	private final ReviewAndGlobalExamUseCases useCases;
	public ReviewAndGlobalExamController(ReviewAndGlobalExamUseCases useCases){this.useCases=useCases;}

	@GetMapping("/subjects/{subjectId}/reviews/due") public List<DueCardResponse> due(@PathVariable UUID subjectId,@CurrentStudent UUID student){return useCases.due(student,subjectId).stream().map(DueCardResponse::from).toList();}
	@PostMapping("/subjects/{subjectId}/review-sessions") public ReviewSessionResponse startReview(@PathVariable UUID subjectId,@CurrentStudent UUID student){return ReviewSessionResponse.from(useCases.startReview(student,subjectId));}
	@PostMapping("/review-sessions/{sessionId}/grades") public GradeResponse grade(@PathVariable UUID sessionId,@CurrentStudent UUID student,@RequestBody GradeRequest request){return GradeResponse.from(useCases.grade(student,sessionId,request.progressId(),request.grade(),request.seconds()));}
	@PostMapping("/review-sessions/{sessionId}/finish") public ReviewSessionResponse finishReview(@PathVariable UUID sessionId,@CurrentStudent UUID student){return ReviewSessionResponse.from(useCases.finishReview(student,sessionId));}
	@PostMapping("/subjects/{subjectId}/global-exams") public GlobalExamStartResponse startExam(@PathVariable UUID subjectId,@CurrentStudent UUID student){return GlobalExamStartResponse.from(useCases.startGlobalExam(student,subjectId));}
	@PostMapping("/global-exams/{examId}/answers/{activityId}") public AnswerRecordedResponse answerExam(@PathVariable UUID examId,@PathVariable UUID activityId,@CurrentStudent UUID student,@RequestBody AnswerRequest request){var answer=useCases.answerGlobalExam(student,examId,activityId,request.optionId(),request.text(),request.seconds());return new AnswerRecordedResponse(answer.id(),answer.activityId(),"Respuesta registrada");}
	@PostMapping("/global-exams/{examId}/finish") public GlobalExamResultResponse finishExam(@PathVariable UUID examId,@CurrentStudent UUID student){return GlobalExamResultResponse.from(useCases.finishGlobalExam(student,examId));}

	public record CardResponse(UUID id,UUID conceptId,String front,String back,String hint){static CardResponse from(ReviewCard c){return new CardResponse(c.id(),c.conceptId(),c.front(),c.back(),c.hint());}}
	public record ProgressResponse(UUID id,double easeFactor,int intervalDays,int repetitions,int totalLapses,int consecutiveLapses,Instant nextReviewAt,String state){static ProgressResponse from(ReviewProgress p){return new ProgressResponse(p.id(),p.easeFactor(),p.intervalDays(),p.repetitions(),p.totalLapses(),p.consecutiveLapses(),p.nextReviewAt(),p.state().name());}}
	public record DueCardResponse(ProgressResponse progress,CardResponse card){static DueCardResponse from(ReviewAndGlobalExamUseCases.DueCard c){return new DueCardResponse(ProgressResponse.from(c.progress()),CardResponse.from(c.card()));}}
	public record ReviewSessionResponse(UUID id,Instant startedAt,Instant finishedAt,int reviewed,int remembered,int forgotten){static ReviewSessionResponse from(ReviewSession s){return new ReviewSessionResponse(s.id(),s.startedAt(),s.finishedAt(),s.reviewed(),s.remembered(),s.forgotten());}}
	public record GradeRequest(UUID progressId,int grade,Integer seconds){}
	public record GradeResponse(UUID progressId,int intervalDays,Instant nextReviewAt,double easeFactor,boolean recalculationTriggered){static GradeResponse from(ReviewAndGlobalExamUseCases.ReviewReceipt r){return new GradeResponse(r.progressId(),r.intervalDays(),r.nextReviewAt(),r.easeFactor(),r.recalculationTriggered());}}
	public record ExamOptionResponse(UUID id,String text){static ExamOptionResponse from(ActivityOption o){return new ExamOptionResponse(o.id(),o.text());}}
	public record ExamActivityResponse(UUID id,UUID conceptId,String code,String statement,int difficulty,String type,List<ExamOptionResponse> options){static ExamActivityResponse from(Activity a){return new ExamActivityResponse(a.id(),a.conceptId(),a.code(),a.statement(),a.difficulty(),a.type().name(),a.options().stream().map(ExamOptionResponse::from).toList());}}
	public record GlobalExamStartResponse(UUID id,int attempt,String state,List<ExamActivityResponse> activities){static GlobalExamStartResponse from(ReviewAndGlobalExamUseCases.GlobalExamStart s){return new GlobalExamStartResponse(s.exam().id(),s.exam().attempt(),s.exam().state(),s.activities().stream().map(ExamActivityResponse::from).toList());}}
	public record AnswerRequest(UUID optionId,String text,Integer seconds){}
	public record AnswerRecordedResponse(UUID id,UUID activityId,String message){}
	public record ConceptSummaryResponse(UUID conceptId,int correct,int total){static ConceptSummaryResponse from(GlobalExamSummary s){return new ConceptSummaryResponse(s.conceptId(),s.correct(),s.total());}}
	public record GlobalExamResultResponse(UUID id,int attempt,double score,boolean passed,List<ConceptSummaryResponse> concepts){static GlobalExamResultResponse from(ReviewAndGlobalExamUseCases.GlobalExamResult r){return new GlobalExamResultResponse(r.exam().id(),r.exam().attempt(),r.exam().score(),Boolean.TRUE.equals(r.exam().passed()),r.concepts().stream().map(ConceptSummaryResponse::from).toList());}}
}
