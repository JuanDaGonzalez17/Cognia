package com.cognia.api.application.service;
import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.application.port.out.*;
import com.cognia.api.domain.model.*;
import com.cognia.api.domain.service.ActivityCorrectionService;
import java.util.*;
import org.junit.jupiter.api.Test;
class PracticeServiceTest {
	private final UUID student=UUID.randomUUID(), aid=UUID.randomUUID(), option=UUID.randomUUID(), wrong=UUID.randomUUID(), concept=UUID.randomUUID();
	private Activity activity=mc(Activity.Usage.PRACTICA,true);
	private final FakeActivities activities=new FakeActivities(); private final FakeAttempts attempts=new FakeAttempts();
	private final PracticeService service=new PracticeService(activities,attempts,id->student.equals(id),id->concept.equals(id),new ActivityCorrectionService());
	@Test void rejectsNonPracticeAndInactiveActivities(){activities.item=mc(Activity.Usage.EVALUACION_CORTA,true); assertThrows(InvalidPracticeRequestException.class,()->service.answer(student,aid,option,null,null)); activities.item=mc(Activity.Usage.PRACTICA,false); assertThrows(InvalidPracticeRequestException.class,()->service.answer(student,aid,option,null,null));}
	@Test void rejectsOptionFromAnotherActivity(){var alienOption=UUID.randomUUID(); assertThrows(InvalidPracticeRequestException.class,()->service.answer(student,aid,alienOption,null,null)); assertTrue(attempts.saved.isEmpty());}
	@Test void excludesNonPracticeAndInactiveFromListings(){activities.list=List.of(activity,mc(Activity.Usage.PRACTICA,false),mc(Activity.Usage.EVALUACION_CORTA,true)); assertEquals(List.of(activity),service.getPracticeActivities(concept,null));}
	@Test void persistsFormativeAnswerAndReturnsFeedback(){var result=service.answer(student,aid,option,null,null); assertTrue(result.correct()); assertEquals("specific",result.specificFeedback()); assertNotNull(result.correctOptionId()); assertEquals(1,attempts.saved.size());}
	@Test void returnsNotFoundForMissingStudent(){assertThrows(ResourceNotFoundException.class,()->service.answer(UUID.randomUUID(),aid,option,null,null));}
	private Activity mc(Activity.Usage usage,boolean active){return new Activity(aid,concept,"A","Q","general",1,usage,Activity.Type.SELECCION_MULTIPLE,active,List.of(new ActivityOption(option,"yes",true,"specific"),new ActivityOption(wrong,"no",false,null)),List.of());}
	private class FakeActivities implements ActivityRepository { List<Activity> list=List.of(activity); Activity item=activity; public List<Activity> findPracticeByConcept(UUID id,Integer difficulty){return list;} public Optional<Activity> findById(UUID id){return id.equals(aid)?Optional.of(item):Optional.empty();} }
	private class FakeAttempts implements PracticeAttemptRepository { List<PracticeAttempt> saved=new ArrayList<>(); public PracticeAttempt save(PracticeAttempt a){saved.add(a);return a;} }
}
