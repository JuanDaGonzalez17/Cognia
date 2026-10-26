package com.cognia.api.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.out.*;
import com.cognia.api.domain.model.*;
import java.sql.Timestamp;
import java.time.Instant;
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
	@BeforeEach void clearStudentReviewRows(){jdbc.update("delete from detalle_respuesta_repaso where sesion_repaso_id in (select id from sesion_repaso where estudiante_id=?)",STUDENT);jdbc.update("delete from sesion_repaso where estudiante_id=?",STUDENT);jdbc.update("delete from progreso_repaso where estudiante_id=?",STUDENT);}

	@Test void activatesMissingCardsListsDueQueueAndPersistsSm2Schedule(){var firstDue=Instant.parse("2026-01-01T00:00:00Z");var cards=deck.activeCards(CONCEPT);deck.addMissingProgress(STUDENT,CONCEPT,cards,firstDue);deck.addMissingProgress(STUDENT,CONCEPT,cards,firstDue);assertEquals(2,jdbc.queryForObject("select count(*) from progreso_repaso where estudiante_id=? and concepto_id=?",Integer.class,STUDENT,CONCEPT));var due=reviews.due(STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),firstDue,1);assertEquals(1,due.size());assertEquals(firstDue,due.get(0).nextReviewAt());var session=reviews.findOrStartSession(STUDENT,UUID.fromString("10000000-0000-0000-0000-000000000001"),firstDue);var next=new ReviewProgress(due.get(0).id(),STUDENT,due.get(0).cardId(),CONCEPT,2.6,1,1,0,0,firstDue,firstDue.plusSeconds(86400),ReviewProgress.State.REPASANDO);reviews.saveGrade(session.id(),next,5,0);assertEquals(1,jdbc.queryForObject("select intervalo_dias from progreso_repaso where id=?",Integer.class,due.get(0).id()));assertEquals(2.6,jdbc.queryForObject("select factor_facilidad_nuevo from detalle_respuesta_repaso where sesion_repaso_id=?",Double.class,session.id()));}

	@Test void databaseRejectsProgressForMismatchedConceptAndEaseFactorBelowFloor(){var card=deck.activeCards(CONCEPT).get(0);assertThrows(DataIntegrityViolationException.class,()->jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,factor_facilidad,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,card.id(),OTHER_CONCEPT,2.5,Timestamp.from(Instant.parse("2026-01-01T00:00:00Z"))));assertThrows(DataIntegrityViolationException.class,()->jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,factor_facilidad,fecha_proximo_repaso) values (?,?,?,?,?)",STUDENT,card.id(),CONCEPT,1.29,Timestamp.from(Instant.parse("2026-01-01T00:00:00Z"))));}
}
