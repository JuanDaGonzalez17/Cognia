package com.cognia.api.domain.service;
import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.domain.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class ActivityCorrectionServiceTest {
	private final ActivityCorrectionService correction=new ActivityCorrectionService();
	private final UUID right=UUID.randomUUID(), wrong=UUID.randomUUID();
	private Activity choice(){return new Activity(UUID.randomUUID(),UUID.randomUUID(),"T1","Q","Feedback",1,Activity.Usage.PRACTICA,Activity.Type.SELECCION_MULTIPLE,true,List.of(new ActivityOption(right,"sí",true,null),new ActivityOption(wrong,"no",false,null)),List.of());}
	@Test void marksCorrectAndIncorrectChoices(){assertTrue(correction.correctChoice(choice(),right)); assertFalse(correction.correctChoice(choice(),wrong));}
	@Test void matchesExactAnswerAfterNormalization(){var a=shortActivity("integral",false); assertTrue(correction.correctShortAnswer(a,"  ÍNTEGRAL "));}
	@Test void matchesRegularExpressionAgainstNormalizedWholeAnswer(){var a=shortActivity("x\\s*\\^\\s*2",true); assertTrue(correction.correctShortAnswer(a," X ^ 2 "));}
	@Test void rejectsEmptyAnswer(){assertThrows(IllegalArgumentException.class,()->correction.correctShortAnswer(shortActivity("x",false),"  "));}
	@Test void rejectsInvalidRegex(){assertThrows(IllegalArgumentException.class,()->new ShortAnswerCriterion("[",true));}
	@Test void evaluatesNestedQuantifierPatternOnBoundedInput(){var a=shortActivity("(a+)+",true); assertTrue(correction.correctShortAnswer(a,"a".repeat(255)));}
	@Test void rejectsInputLongerThanDatabasePatternLimit(){assertThrows(IllegalArgumentException.class,()->correction.correctShortAnswer(shortActivity("a",false),"a".repeat(256)));}
	private Activity shortActivity(String pattern,boolean regex){return new Activity(UUID.randomUUID(),UUID.randomUUID(),"S","Q","F",2,Activity.Usage.PRACTICA,Activity.Type.RESPUESTA_CORTA,true,List.of(),List.of(new ShortAnswerCriterion(pattern,regex)));}
}
