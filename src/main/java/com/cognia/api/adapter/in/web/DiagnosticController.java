package com.cognia.api.adapter.in.web;
import com.cognia.api.adapter.in.security.CurrentStudent;
import com.cognia.api.application.port.in.DiagnosticUseCases; import com.cognia.api.domain.model.*; import java.util.*; import org.springframework.web.bind.annotation.*;
@RestController public class DiagnosticController {
	private final DiagnosticUseCases useCases; public DiagnosticController(DiagnosticUseCases useCases){this.useCases=useCases;}
	@PostMapping("/api/v1/subjects/{subjectId}/diagnostics") public StartResponse start(@PathVariable UUID subjectId,@CurrentStudent UUID studentId){return StartResponse.from(useCases.start(studentId,subjectId));}
	@PostMapping("/api/v1/diagnostics/{sessionId}/answers/{activityId}") public AnswerResponse answer(@PathVariable UUID sessionId,@PathVariable UUID activityId,@CurrentStudent UUID studentId,@RequestBody AnswerRequest request){var a=useCases.answer(studentId,sessionId,activityId,request.optionId(),request.answer(),request.seconds());return AnswerResponse.from(a);}
	@PostMapping("/api/v1/diagnostics/{sessionId}/finish") public SessionResponse finish(@PathVariable UUID sessionId,@CurrentStudent UUID studentId){return SessionResponse.from(useCases.finish(studentId,sessionId));}
	@GetMapping("/api/v1/students/{studentId}/subjects/{subjectId}/knowledge-profile") public ProfileResponse profile(@PathVariable UUID studentId,@PathVariable UUID subjectId,@CurrentStudent UUID currentStudent){if(!currentStudent.equals(studentId))throw new com.cognia.api.application.service.DiagnosticConflictException("Student id does not match authenticated student");return ProfileResponse.from(useCases.profile(studentId,subjectId));}
	public record StartResponse(SessionResponse session,List<ActivityResponse> activities){static StartResponse from(DiagnosticUseCases.DiagnosticStart s){return new StartResponse(SessionResponse.from(s.session()),s.activities().stream().map(ActivityResponse::from).toList());}}
	public record SessionResponse(UUID id,UUID subjectId,String state,Double score,java.time.Instant closedAt){static SessionResponse from(DiagnosticSession s){return new SessionResponse(s.id(),s.subjectId(),s.state(),s.score(),s.closedAt());}}
	public record ActivityResponse(UUID id,UUID conceptId,String code,String statement,int difficulty,String type,List<OptionResponse> options){static ActivityResponse from(Activity a){return new ActivityResponse(a.id(),a.conceptId(),a.code(),a.statement(),a.difficulty(),a.type().name(),a.options().stream().map(OptionResponse::from).toList());}}
	public record OptionResponse(UUID id,String text){static OptionResponse from(ActivityOption o){return new OptionResponse(o.id(),o.text());}}
	public record AnswerRequest(UUID optionId,String answer,Integer seconds){}
	public record AnswerResponse(UUID id,UUID activityId,String recorded){static AnswerResponse from(DiagnosticAnswer a){return new AnswerResponse(a.id(),a.activityId(),"Respuesta registrada");}}
	public record ProfileResponse(UUID id,UUID studentId,UUID subjectId,boolean diagnosticCompleted,List<MasteryResponse> concepts){static ProfileResponse from(KnowledgeProfile p){return new ProfileResponse(p.id(),p.studentId(),p.subjectId(),p.diagnosticCompleted(),p.masteries().stream().map(MasteryResponse::from).toList());}}
	public record MasteryResponse(UUID conceptId,double mastery,int totalEvaluations,int totalCorrect){static MasteryResponse from(ConceptMastery m){return new MasteryResponse(m.conceptId(),m.level(),m.totalEvaluations(),m.totalCorrect());}}
}
