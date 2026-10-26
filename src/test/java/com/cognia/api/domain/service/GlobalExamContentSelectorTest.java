package com.cognia.api.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.domain.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class GlobalExamContentSelectorTest {
	private final UUID c1=UUID.randomUUID(),c2=UUID.randomUUID();
	private Activity item(String code,UUID concept,boolean active,Activity.Usage usage){return new Activity(UUID.randomUUID(),concept,code,"Question "+code,null,1,usage,Activity.Type.RESPUESTA_CORTA,active,List.of(),List.of(new ShortAnswerCriterion("x",false)));}
	@Test void selectsActiveGlobalItemsWithinConceptLimitInStableConceptAndCodeOrder(){var items=List.of(item("B",c1,true,Activity.Usage.EXAMEN_GLOBAL),item("A",c1,true,Activity.Usage.EXAMEN_GLOBAL),item("C",c1,true,Activity.Usage.EXAMEN_GLOBAL),item("D",c2,true,Activity.Usage.EXAMEN_GLOBAL),item("E",c2,false,Activity.Usage.EXAMEN_GLOBAL),item("F",c2,true,Activity.Usage.PRACTICA));var selected=new GlobalExamContentSelector().select(items,Map.of(c1,2,c2,1),2);assertEquals(List.of("D","A","B"),selected.stream().map(Activity::code).toList());}
	@Test void rejectsNonpositivePerConceptLimit(){assertThrows(IllegalArgumentException.class,()->new GlobalExamContentSelector().select(List.of(),Map.of(),0));}
}
