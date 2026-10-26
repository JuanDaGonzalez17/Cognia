package com.cognia.api.application.service;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.in.LearningPathUseCases;
import com.cognia.api.application.port.out.ReviewDeckPort;
import com.cognia.api.domain.model.ReviewCard;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest @ActiveProfiles("test") @Import({TestcontainersConfiguration.class,LearningPathReviewAtomicityTest.FailDeckConfiguration.class})
@Sql({"/db/seed-calculo-integral.sql","/db/seed-actividades.sql","/db/seed-diagnostico.sql","/db/seed-evaluacion-corta.sql","/db/seed-repaso-examen.sql"})
class LearningPathReviewAtomicityTest {
	private static final UUID STUDENT=UUID.fromString("30000000-0000-0000-0000-000000000001"), SUBJECT=UUID.fromString("10000000-0000-0000-0000-000000000001"), CONCEPT=UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID PATH=UUID.fromString("85000000-0000-0000-0000-000000000011"), STEP=UUID.fromString("86000000-0000-0000-0000-000000000011"), SESSION=UUID.fromString("87000000-0000-0000-0000-000000000011"), PROFILE=UUID.fromString("88000000-0000-0000-0000-000000000011");
	@Autowired JdbcTemplate jdbc; @Autowired LearningPathUseCases learningPath;
	@BeforeEach void prepareApprovedEvaluation(){jdbc.update("insert into perfil_conocimiento(id,estudiante_id,asignatura_id,diagnostico_completado) values (?,?,?,true)",PROFILE,STUDENT,SUBJECT);jdbc.update("insert into dominio_conceptual(perfil_id,concepto_id,nivel_dominio,total_evaluaciones,total_aciertos) values (?,?,0.5,1,1)",PROFILE,CONCEPT);jdbc.update("insert into ruta_aprendizaje(id,estudiante_id,asignatura_id,version,estado) values (?,?,?,1,'ACTIVA')",PATH,STUDENT,SUBJECT);jdbc.update("insert into paso_ruta(id,ruta_id,concepto_id,orden_sugerido,estado) values (?,?,?,0,'EN_PROGRESO')",STEP,PATH,CONCEPT);jdbc.update("insert into sesion_evaluacion(id,estudiante_id,tipo_evaluacion,asignatura_id,concepto_id,paso_ruta_id,intento_numero,estado) values (?,?, 'EVALUACION_CORTA',?,?,?,1,'EN_PROGRESO')",SESSION,STUDENT,SUBJECT,CONCEPT,STEP);jdbc.update("insert into detalle_respuesta_evaluacion(sesion_evaluacion_id,actividad_id,opcion_seleccionada_id,es_correcta) values (?,?,?,true)",SESSION,UUID.fromString("42000000-0000-0000-0000-000000000001"),UUID.fromString("52000000-0000-0000-0000-000000000001"));}
	@Test void approvalAndDeckEnrollmentRollBackAsOneTransactionWhenEnrollmentFails(){assertThrows(IllegalStateException.class,()->learningPath.finish(STUDENT,SESSION));assertEquals("EN_PROGRESO",jdbc.queryForObject("select estado from sesion_evaluacion where id=?",String.class,SESSION));assertEquals("EN_PROGRESO",jdbc.queryForObject("select estado from paso_ruta where id=?",String.class,STEP));assertEquals(0.5,jdbc.queryForObject("select nivel_dominio from dominio_conceptual where perfil_id=? and concepto_id=?",Double.class,PROFILE,CONCEPT));assertEquals(0,jdbc.queryForObject("select count(*) from historial_dominio where referencia_id=?",Integer.class,SESSION));assertEquals(0,jdbc.queryForObject("select count(*) from progreso_repaso where estudiante_id=? and concepto_id=?",Integer.class,STUDENT,CONCEPT));}
	@TestConfiguration(proxyBeanMethods=false) static class FailDeckConfiguration {@Bean @Primary ReviewDeckPort failingDeck(){return new ReviewDeckPort(){public List<ReviewCard> activeCards(UUID concept){return List.of(new ReviewCard(UUID.fromString("70000000-0000-0000-0000-000000000001"),concept,"front","back",null));}public Optional<ReviewCard> card(UUID id){return Optional.empty();}public void addMissingProgress(UUID student,UUID concept,List<ReviewCard> cards,Instant due){throw new IllegalStateException("forced deck failure");}public void resetConsecutiveLapses(UUID student,UUID concept){}};}}
}
