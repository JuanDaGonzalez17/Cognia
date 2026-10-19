package com.cognia.api.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cognia.api.TestcontainersConfiguration;
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
@Sql("/db/seed-calculo-integral.sql")
class SubjectControllerTest {
	private static final String SUBJECT_ID = "10000000-0000-0000-0000-000000000001";
	private static final String FIRST_CONCEPT_ID = "20000000-0000-0000-0000-000000000001";
	private static final String SECOND_CONCEPT_ID = "20000000-0000-0000-0000-000000000002";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsSubjectAndConcepts() throws Exception {
		mockMvc.perform(get("/api/v1/subjects/" + SUBJECT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("calculo-integral"));
		mockMvc.perform(get("/api/v1/subjects/" + SUBJECT_ID + "/concepts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(4))
				.andExpect(jsonPath("$[0].level").value(0));
	}

	@Test
	void returnsPrerequisitesForConcept() throws Exception {
		mockMvc.perform(get("/api/v1/concepts/" + SECOND_CONCEPT_ID + "/prerequisites"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].prerequisiteId").value(FIRST_CONCEPT_ID));
	}

	@Test
	void returnsProblemDetailNotFoundForUnknownConcept() throws Exception {
		mockMvc.perform(get("/api/v1/concepts/00000000-0000-0000-0000-000000000099/prerequisites"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Resource not found"));
	}

	@Test
	void returns422ForSelfPrerequisiteAnd409ForCycle() throws Exception {
		mockMvc.perform(post("/api/v1/concepts/" + FIRST_CONCEPT_ID + "/prerequisites")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"prerequisiteId\":\"" + FIRST_CONCEPT_ID + "\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value(422));

		mockMvc.perform(post("/api/v1/concepts/" + FIRST_CONCEPT_ID + "/prerequisites")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"prerequisiteId\":\"" + SECOND_CONCEPT_ID + "\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}
}
