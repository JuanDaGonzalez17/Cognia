package com.cognia.api.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cognia.api.TestcontainersConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Sql({"/db/seed-calculo-integral.sql", "/db/seed-actividades.sql"})
class PracticeControllerTest {
	private static final String CONCEPT = "20000000-0000-0000-0000-000000000001";
	private static final String ACTIVITY = "40000000-0000-0000-0000-000000000001";
	private static final String STUDENT = "30000000-0000-0000-0000-000000000001";

	@Autowired
	MockMvc mvc;

	@Test
	void queryNeverExposesCorrectnessOrSpecificFeedback() throws Exception {
		mvc.perform(get("/api/v1/concepts/{id}/practice-activities", CONCEPT)
				.header("X-Student-Id", STUDENT))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].options[0].correct").doesNotExist())
				.andExpect(jsonPath("$[0].options[0].specificFeedback").doesNotExist())
				.andExpect(content().string(Matchers.not(Matchers.containsString("es_correcta"))))
				.andExpect(content().string(Matchers.not(Matchers.containsString("retroalimentacion_especifica"))));
	}

	@Test
	void answersActivityAndReturnsCorrectOptionOnlyAfterAnswer() throws Exception {
		mvc.perform(post("/api/v1/activities/{id}/practice-attempts", ACTIVITY)
				.header("X-Student-Id", STUDENT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"optionId\":\"50000000-0000-0000-0000-000000000001\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(true))
				.andExpect(jsonPath("$.correctOptionId").value("50000000-0000-0000-0000-000000000001"));
	}

	@Test
	void missingOrInvalidStudentIdentityReturnsUnauthorizedProblem() throws Exception {
		mvc.perform(post("/api/v1/activities/{id}/practice-attempts", ACTIVITY)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401));

		mvc.perform(post("/api/v1/activities/{id}/practice-attempts", ACTIVITY)
				.header("X-Student-Id", "invalid")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void invalidPracticeRequestUses422() throws Exception {
		mvc.perform(post("/api/v1/activities/{id}/practice-attempts",
					"40000000-0000-0000-0000-000000000009")
				.header("X-Student-Id", STUDENT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"optionId\":\"50000000-0000-0000-0000-000000000009\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.title").value("Invalid practice request"));
	}
}
