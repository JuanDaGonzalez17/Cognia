package com.cognia.api.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SubjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsSeededSubjectWithConcepts() throws Exception {
		mockMvc.perform(get("/api/v1/subjects/integral-calculus"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("integral-calculus"))
				.andExpect(jsonPath("$.concepts.length()").value(3));
	}

	@Test
	void returnsNotFoundForUnknownSubject() throws Exception {
		mockMvc.perform(get("/api/v1/subjects/unknown"))
				.andExpect(status().isNotFound());
	}
}
