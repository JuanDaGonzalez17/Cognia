package com.cognia.api.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.out.*;
import com.cognia.api.domain.model.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest @ActiveProfiles("test") @Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql","/db/seed-actividades.sql","/db/seed-diagnostico.sql","/db/seed-evaluacion-corta.sql","/db/seed-repaso-examen.sql"})
class ReviewPersistenceTest {
	private static final UUID STUDENT=UUID.fromString("30000000-0000-0000-0000-000000000001");
	private static final UUID CONCEPT=UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID OTHER_CONCEPT=UUID.fromString("20000000-0000-0000-0000-000000000002");
	@Autowired ReviewDeckPort deck;
	@Autowired ReviewRepository reviews;
	@Autowired JdbcTemplate jdbc;
	@Autowired SubjectActivityQueriesPort activityQueries;
	@BeforeEach void clearStudentReviewRows(){jdbc.update("delete from detalle_respuesta_repaso where sesion_repaso_id in (select id from sesion_repaso where estudiante_id=?)",STUDENT);jdbc.update("delete from sesion_repaso where estudiante_id=?",STUDENT);jdbc.update("delete from progreso_repaso where estudiante_id=?",STUDENT);jdbc.update("delete from detalle_respuesta_evaluacion where sesion_evaluacion_id in (select id from sesion_evaluacion where estudiante_id=?)",STUDENT);jdbc.update("delete from sesion_evaluacion where estudiante_id=?",STUDENT);jdbc.update("delete from ruta_aprendizaje where id='85000000-0000-0000-0000-000000000031'");}

	@Test void activatesMissingCardsListsDueQueueAndPersistsSm2Schedule(){var firstDue=Instant.parse("2026-01-01T00:00:00Z");var cards=deck.activeCards(CONCEPT);deck.addMissingProgress(STUDENT,CONCEPT,cards,firstDue);deck.addMissingProgress(STUDENT,CONCEPT,cards,firstDue);assertEquals(2,jdbc.queryForObject("select count(*) from progreso_repaso where estudiante_id=? and concepto_id=?",Integer.class,STUDENT,CONCEPT));var due=reviews.due(STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),firstDue,1);assertEquals(1,due.size());assertEquals(firstDue,due.get(0).nextReviewAt());var session=reviews.findOrStartSession(STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),firstDue);var next=new ReviewProgress(due.get(0).id(),STUDENT,due.get(0).cardId(),CONCEPT,2.6,1,1,0,0,firstDue,firstDue.plusSeconds(86400),ReviewProgress.State.REPASANDO);reviews.saveGrade(session.id(),next,5,0);assertEquals(1,jdbc.queryForObject("select intervalo_dias from progreso_repaso where id=?",Integer.class,due.get(0).id()));assertEquals(2.6,jdbc.queryForObject("select factor_facilidad_nuevo from detalle_respuesta_repaso where sesion_repaso_id=?",Double.class,session.id()));}

	@Test void databaseRejectsProgressForMismatchedConceptAndEaseFactorBelowFloor(){var card=deck.activeCards(CONCEPT).get(0);assertThrows(DataIntegrityViolationException.class,()->jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,factor_facilidad,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,card.id(),OTHER_CONCEPT,2.5,Timestamp.from(Instant.parse("2026-01-01T00:00:00Z"))));assertThrows(DataIntegrityViolationException.class,()->jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,factor_facilidad,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,card.id(),CONCEPT,1.29,Timestamp.from(Instant.parse("2026-01-01T00:00:00Z"))));}

	@Test void historyReturnsOnlyCompletedEvaluationsAndFinishedReviewsInDateOrder(){
		var now=Instant.parse("2026-10-28T12:00:00Z");
		var path=UUID.fromString("85000000-0000-0000-0000-000000000031");
		var step=UUID.fromString("86000000-0000-0000-0000-000000000031");
		jdbc.update("insert into ruta_aprendizaje(id,estudiante_id,asignatura_id,version,estado) values (?,?,?,1,'ACTIVA')",path,STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"));
		jdbc.update("insert into paso_ruta(id,ruta_id,concepto_id,orden_sugerido,estado) values (?,?,?,0,'DISPONIBLE')",step,path,CONCEPT);
		insertEvaluation("DIAGNOSTICO",null,null,null,now.minusSeconds(300),true);
		insertEvaluation("EVALUACION_CORTA",CONCEPT,step,true,now.minusSeconds(200),true);
		insertEvaluation("EXAMEN_GLOBAL",null,null,true,now.minusSeconds(100),true);
		insertEvaluation("EXAMEN_GLOBAL",null,null,true,null,false);
		jdbc.update("insert into sesion_repaso(id,estudiante_id,asignatura_id,fecha_inicio,fecha_fin,total_tarjetas_revisadas,total_recordadas,total_olvidadas) values (?,?,?,?,?,4,3,1)",UUID.randomUUID(),STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),Timestamp.from(now.minusSeconds(80)),Timestamp.from(now.minusSeconds(50)));
		jdbc.update("insert into sesion_repaso(id,estudiante_id,asignatura_id,fecha_inicio,fecha_fin) values (?,?,?,?,?)",UUID.randomUUID(),STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),Timestamp.from(now.minusSeconds(45)),Timestamp.from(now.minusSeconds(40)));
		jdbc.update("insert into sesion_repaso(id,estudiante_id,asignatura_id,fecha_inicio) values (?,?,?,?)",UUID.randomUUID(),STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),Timestamp.from(now.minusSeconds(20)));
		var history=activityQueries.history(STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),50);
		assertEquals(List.of("REPASO","REPASO","EXAMEN_GLOBAL","EVALUACION_CORTA","DIAGNOSTICO"),history.stream().map(com.cognia.api.application.port.in.SubjectActivityQueries.Activity::type).toList());
		assertNull(history.get(0).score());
		assertEquals(75d,history.get(1).score());
		assertEquals("Diagnóstico inicial",history.get(4).conceptName());
	}

	@Test void upcomingReturnsOnlyFutureProgressForActiveCardsInAscendingOrder(){
		var now=Instant.parse("2026-10-28T12:00:00Z");
		var cards=deck.activeCards(CONCEPT);
		jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,intervalo_dias,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,cards.get(0).id(),CONCEPT,2,Timestamp.from(now.plusSeconds(3600)));
		jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,intervalo_dias,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,cards.get(1).id(),CONCEPT,5,Timestamp.from(now.minusSeconds(3600)));
		var inactiveCard=UUID.randomUUID();
		jdbc.update("insert into tarjeta_repaso(id,concepto_id,codigo,anverso_pregunta,reverso_respuesta,activa) values (?,?,?,'Q','A',false)",inactiveCard,CONCEPT,"inactive-"+inactiveCard.toString().substring(0,8));
		jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,intervalo_dias,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,inactiveCard,CONCEPT,7,Timestamp.from(now.plusSeconds(7200)));
		var upcoming=activityQueries.upcoming(STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),now,100);
		assertEquals(1,upcoming.size());
		assertEquals(2,upcoming.get(0).intervalDays());
	}

	private void insertEvaluation(String type,UUID concept,UUID step,Boolean passed,Instant finished,boolean complete){
		jdbc.update("insert into sesion_evaluacion(estudiante_id,tipo_evaluacion,asignatura_id,concepto_id,paso_ruta_id,fecha_finalizacion,puntaje_obtenido,aprobada,estado) values (?,?,?,?,?,?,?,?,?)",
			STUDENT,type,UUID.fromString("10000000-0000-0000-0000-000000000001"),concept,step,finished==null?null:Timestamp.from(finished),complete?80d:null,passed,complete?"COMPLETADA":"EN_PROGRESO");
	}
}
