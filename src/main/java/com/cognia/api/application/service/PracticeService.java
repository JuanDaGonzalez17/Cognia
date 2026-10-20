package com.cognia.api.application.service;
import com.cognia.api.application.port.in.AnswerPracticeActivityUseCase;
import com.cognia.api.application.port.in.GetPracticeActivitiesUseCase;
import com.cognia.api.application.port.out.ActivityRepository;
import com.cognia.api.application.port.out.PracticeAttemptRepository;
import com.cognia.api.application.port.out.StudentExistencePort;
import com.cognia.api.application.port.out.ConceptExistencePort;
import com.cognia.api.domain.model.Activity;
import com.cognia.api.domain.model.PracticeAttempt;
import com.cognia.api.domain.model.PracticeResult;
import com.cognia.api.domain.service.ActivityCorrectionService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
@Service
public class PracticeService implements GetPracticeActivitiesUseCase, AnswerPracticeActivityUseCase {
	private final ActivityRepository activities; private final PracticeAttemptRepository attempts; private final StudentExistencePort students; private final ConceptExistencePort concepts; private final ActivityCorrectionService correction;
	public PracticeService(ActivityRepository activities, PracticeAttemptRepository attempts, StudentExistencePort students, ConceptExistencePort concepts, ActivityCorrectionService correction) { this.activities=activities; this.attempts=attempts; this.students=students; this.concepts=concepts; this.correction=correction; }
	public List<Activity> getPracticeActivities(UUID conceptId, Integer difficulty) { if (!concepts.exists(conceptId)) throw new ResourceNotFoundException("Concept " + conceptId + " was not found"); if (difficulty != null && (difficulty < 1 || difficulty > 3)) throw new InvalidPracticeRequestException("Difficulty must be between 1 and 3"); return activities.findPracticeByConcept(conceptId, difficulty).stream().filter(a -> a.usage()==Activity.Usage.PRACTICA && a.active()).toList(); }
	public PracticeResult answer(UUID studentId, UUID activityId, UUID optionId, String answer, Integer seconds) {
		if (!students.exists(studentId)) throw new ResourceNotFoundException("Student " + studentId + " was not found");
		var activity=activities.findById(activityId).orElseThrow(()->new ResourceNotFoundException("Activity " + activityId + " was not found"));
		if (activity.usage()!=Activity.Usage.PRACTICA || !activity.active()) throw new InvalidPracticeRequestException("Activity is not an active practice activity");
		if (seconds != null && seconds < 0) throw new InvalidPracticeRequestException("Seconds must not be negative");
		boolean correct; String chosenFeedback=null; UUID correctOption=null; String storedAnswer=answer;
		try { if(activity.type()==Activity.Type.SELECCION_MULTIPLE) { if(optionId==null || answer!=null) throw new IllegalArgumentException("Select exactly one option"); var option=activity.options().stream().filter(o->o.id().equals(optionId)).findFirst().orElseThrow(()->new IllegalArgumentException("Option does not belong to activity")); correct=correction.correctChoice(activity,optionId); chosenFeedback=option.specificFeedback(); storedAnswer=option.text(); correctOption=activity.options().stream().filter(o->o.correct()).findFirst().orElseThrow().id(); } else { if(optionId!=null) throw new IllegalArgumentException("Short answer cannot include an option"); correction.validateAnswer(answer); correct=correction.correctShortAnswer(activity,answer); storedAnswer=answer; } } catch(IllegalArgumentException ex) { throw new InvalidPracticeRequestException(ex.getMessage()); }
		attempts.save(new PracticeAttempt(null,studentId,activityId,optionId,storedAnswer,correct,seconds));
		return new PracticeResult(correct,activity.feedback(),chosenFeedback,correctOption);
	}
}
