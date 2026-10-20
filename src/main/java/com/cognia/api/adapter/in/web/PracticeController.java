package com.cognia.api.adapter.in.web;
import com.cognia.api.application.port.in.*; import com.cognia.api.domain.model.*; import java.util.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController public class PracticeController {
	private final GetPracticeActivitiesUseCase getActivities; private final AnswerPracticeActivityUseCase answerActivity;
	public PracticeController(GetPracticeActivitiesUseCase getActivities,AnswerPracticeActivityUseCase answerActivity){this.getActivities=getActivities;this.answerActivity=answerActivity;}
	@GetMapping("/api/v1/concepts/{conceptId}/practice-activities") public List<ActivityResponse> getActivities(@PathVariable UUID conceptId,@RequestParam(required=false) Integer difficulty){return getActivities.getPracticeActivities(conceptId,difficulty).stream().map(ActivityResponse::from).toList();}
	@PostMapping("/api/v1/activities/{activityId}/practice-attempts") public PracticeResultResponse answer(@PathVariable UUID activityId,@RequestHeader("X-Student-Id") UUID studentId,@RequestBody AnswerRequest request){return PracticeResultResponse.from(answerActivity.answer(studentId,activityId,request.optionId(),request.answer(),request.seconds()));}
	public record ActivityResponse(UUID id,UUID conceptId,String code,String statement,int difficulty,String type,List<OptionResponse> options){static ActivityResponse from(Activity a){return new ActivityResponse(a.id(),a.conceptId(),a.code(),a.statement(),a.difficulty(),a.type().name(),a.options().stream().map(OptionResponse::from).toList());}}
	public record OptionResponse(UUID id,String text){static OptionResponse from(ActivityOption o){return new OptionResponse(o.id(),o.text());}}
	public record AnswerRequest(UUID optionId,String answer,Integer seconds){}
	public record PracticeResultResponse(boolean correct,String feedback,String specificFeedback,UUID correctOptionId){static PracticeResultResponse from(PracticeResult r){return new PracticeResultResponse(r.correct(),r.feedback(),r.specificFeedback(),r.correctOptionId());}}
}
