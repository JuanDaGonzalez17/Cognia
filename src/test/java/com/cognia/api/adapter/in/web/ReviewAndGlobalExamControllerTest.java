package com.cognia.api.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.cognia.api.TestcontainersConfiguration;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql","/db/seed-actividades.sql","/db/seed-diagnostico.sql","/db/seed-evaluacion-corta.sql","/db/seed-repaso-examen.sql"})
class ReviewAndGlobalExamControllerTest {
	private static final java.util.UUID STUDENT=java.util.UUID.fromString("30000000-0000-0000-0000-000000000001");
	private static final java.util.UUID SUBJECT=java.util.UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final java.util.UUID CONCEPT=java.util.UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final java.util.UUID CARD=java.util.UUID.fromString("70000000-0000-0000-0000-000000000001");
	@Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc;
	@BeforeEach void addDueCard(){jdbc.update("delete from detalle_respuesta_repaso where sesion_repaso_id in (select id from sesion_repaso where estudiante_id=?)",STUDENT);jdbc.update("delete from sesion_repaso where estudiante_id=?",STUDENT);jdbc.update("delete from progreso_repaso where estudiante_id=?",STUDENT);jdbc.update("delete from detalle_respuesta_evaluacion where sesion_evaluacion_id in (select id from sesion_evaluacion where estudiante_id=? and tipo_evaluacion='EXAMEN_GLOBAL')",STUDENT);jdbc.update("delete from sesion_evaluacion where estudiante_id=? and tipo_evaluacion='EXAMEN_GLOBAL'",STUDENT);jdbc.update("delete from paso_ruta where ruta_id in ('85000000-0000-0000-0000-000000000001','85000000-0000-0000-0000-000000000002')");jdbc.update("delete from ruta_aprendizaje where id in ('85000000-0000-0000-0000-000000000001','85000000-0000-0000-0000-000000000002')");jdbc.update("insert into progreso_repaso(estudiante_id,tarjeta_id,concepto_id,fecha_proximo_repaso) values (?,?,?,?)",STUDENT,CARD,CONCEPT,Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")));}

	@Test void reviewQueueIncludesTheBackAndGradeReturnsUpdatedSchedule() throws Exception {mvc.perform(get("/api/v1/subjects/{subject}/reviews/due",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andExpect(jsonPath("$[0].card.front").exists()).andExpect(jsonPath("$[0].card.back").exists());var session=mvc.perform(post("/api/v1/subjects/{subject}/review-sessions",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();var matcher=java.util.regex.Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"").matcher(session);if(!matcher.find())throw new AssertionError("Review session response did not contain an id");var sessionId=matcher.group(1);mvc.perform(post("/api/v1/review-sessions/{id}/grades",sessionId).header("X-Student-Id",STUDENT).contentType(MediaType.APPLICATION_JSON).content("{\"progressId\":\""+jdbc.queryForObject("select id from progreso_repaso where estudiante_id=? and tarjeta_id=?",String.class,STUDENT,CARD)+"\",\"grade\":5}")).andExpect(status().isOk()).andExpect(jsonPath("$.intervalDays").value(1)).andExpect(jsonPath("$.nextReviewAt").exists());}
	@Test void globalExamDoesNotExposeQuestionCorrection() throws Exception {insertCompletedPath();mvc.perform(post("/api/v1/subjects/{subject}/global-exams",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andExpect(jsonPath("$.activities[0].options[0].text").exists()).andExpect(jsonPath("$.activities[0].options[0].correct").doesNotExist()).andExpect(jsonPath("$.activities[0].criteria").doesNotExist());}
	@Test void activeReopenedVersionDisablesExamDespitePreviousCompletedVersion() throws Exception {insertCompletedPath();jdbc.update("update ruta_aprendizaje set estado='RECALCULADA' where estudiante_id=? and asignatura_id=?",STUDENT,SUBJECT);jdbc.update("insert into ruta_aprendizaje(id,estudiante_id,asignatura_id,version,estado) values ('85000000-0000-0000-0000-000000000002',?,?,2,'ACTIVA')",STUDENT,SUBJECT);jdbc.update("insert into paso_ruta(id,ruta_id,concepto_id,orden_sugerido,estado) values ('86000000-0000-0000-0000-000000000005','85000000-0000-0000-0000-000000000002',?,?, 'REFUERZO')",CONCEPT,0);mvc.perform(post("/api/v1/subjects/{subject}/global-exams",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));}
	@Test void missingCompletedRouteReturnsConflict() throws Exception {mvc.perform(post("/api/v1/subjects/{subject}/global-exams",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isConflict());}
	private void insertCompletedPath(){jdbc.update("insert into ruta_aprendizaje(id,estudiante_id,asignatura_id,version,estado) values ('85000000-0000-0000-0000-000000000001',?,?,1,'COMPLETADA')",STUDENT,SUBJECT);for(int i=1;i<=4;i++){var conceptId=java.util.UUID.fromString("20000000-0000-0000-0000-%012d".formatted(i));var stepId=java.util.UUID.fromString("86000000-0000-0000-0000-%012d".formatted(i));jdbc.update("insert into paso_ruta(id,ruta_id,concepto_id,orden_sugerido,estado) values (?,?,?,?, 'DOMINADO')",stepId,java.util.UUID.fromString("85000000-0000-0000-0000-000000000001"),conceptId,i-1);}}
}
