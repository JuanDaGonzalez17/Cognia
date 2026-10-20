package com.cognia.api.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.cognia.api.TestcontainersConfiguration;
import com.cognia.api.application.port.out.DiagnosticRepository;
import com.cognia.api.domain.model.DiagnosticAnswer;
import com.cognia.api.domain.model.DiagnosticSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql","/db/seed-actividades.sql","/db/seed-diagnostico.sql"})
class DiagnosticControllerTest {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;
	@Autowired DiagnosticRepository diagnostics;
	private static final String STUDENT="30000000-0000-0000-0000-000000000001", SUBJECT="10000000-0000-0000-0000-000000000001", ACTIVITY="41000000-0000-0000-0000-000000000001", OPTION="51000000-0000-0000-0000-000000000001";
	@BeforeEach void clearDiagnosticFixtures(){jdbc.update("delete from sesion_evaluacion where estudiante_id=? and tipo_evaluacion='DIAGNOSTICO'",java.util.UUID.fromString(STUDENT));jdbc.update("delete from perfil_conocimiento where estudiante_id=?",java.util.UUID.fromString(STUDENT));}
	@Test void diagnosticFlowDoesNotRevealCorrectnessAndReturnsEvidenceCounts() throws Exception {
		var started=mvc.perform(post("/api/v1/subjects/{id}/diagnostics",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andExpect(jsonPath("$.activities[0].options[0].correct").doesNotExist()).andReturn();
		var id=sessionId(started.getResponse().getContentAsString());
		mvc.perform(post("/api/v1/diagnostics/{session}/answers/{activity}",id,ACTIVITY).header("X-Student-Id",STUDENT).contentType(MediaType.APPLICATION_JSON).content("{\"optionId\":\""+OPTION+"\"}"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.correct").doesNotExist()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("correctOptionId"))));
		mvc.perform(post("/api/v1/diagnostics/{session}/answers/{activity}",id,ACTIVITY).header("X-Student-Id",STUDENT).contentType(MediaType.APPLICATION_JSON).content("{\"optionId\":\""+OPTION+"\"}")).andExpect(status().isConflict());
		mvc.perform(post("/api/v1/diagnostics/{id}/finish",id).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andExpect(jsonPath("$.state").value("COMPLETADA")).andExpect(jsonPath("$.score").value(100));
		mvc.perform(get("/api/v1/students/{student}/subjects/{subject}/knowledge-profile",STUDENT,SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andExpect(jsonPath("$.diagnosticCompleted").value(true)).andExpect(jsonPath("$.concepts[0].totalEvaluations").value(1)).andExpect(jsonPath("$.concepts[0].totalCorrect").value(1));
	}
	@Test void finishingWithoutResponsesReturns422AndKeepsSessionOpen() throws Exception {
		var started=mvc.perform(post("/api/v1/subjects/{id}/diagnostics",SUBJECT).header("X-Student-Id",STUDENT)).andExpect(status().isOk()).andReturn();var id=sessionId(started.getResponse().getContentAsString());
		mvc.perform(post("/api/v1/diagnostics/{id}/finish",id).header("X-Student-Id",STUDENT)).andExpect(status().isUnprocessableEntity());
	}
	@Test void databaseRejectsDuplicateActivityAnswerInOneSession(){var session=diagnostics.saveSession(new DiagnosticSession(null,java.util.UUID.fromString(STUDENT),java.util.UUID.fromString(SUBJECT),"EN_PROGRESO",null,null));var activity=java.util.UUID.fromString(ACTIVITY);var option=java.util.UUID.fromString(OPTION);diagnostics.saveAnswer(new DiagnosticAnswer(null,session.id(),activity,option,null,true,null));assertThrows(DataIntegrityViolationException.class,()->diagnostics.saveAnswer(new DiagnosticAnswer(null,session.id(),activity,option,null,true,null)));}
	private String sessionId(String json){var prefix="\"session\":{\"id\":\"";var start=json.indexOf(prefix)+prefix.length();return json.substring(start,json.indexOf('"',start));}
}
