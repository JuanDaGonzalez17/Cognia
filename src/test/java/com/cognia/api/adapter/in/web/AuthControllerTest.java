package com.cognia.api.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cognia.api.TestcontainersConfiguration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {
	@Autowired private MockMvc mvc;

	@Test
	void registersLogsInAndReadsTheAuthenticatedProfile() throws Exception {
		var email = "student-" + UUID.randomUUID() + "@example.invalid";
		var password = "SafePass123!";
		var registered = mvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"fullName\":\"Test Student\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value(email))
			.andReturn().getResponse().getContentAsString();
		var studentIdMatcher = Pattern.compile("\\\"studentId\\\":\\\"([^\\\"]+)\\\"").matcher(registered);
		if (!studentIdMatcher.find()) throw new AssertionError("Registration response did not contain a student id");

		var loggedIn = mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.studentId").value(studentIdMatcher.group(1)))
			.andExpect(jsonPath("$.expiresIn").value(86400))
			.andReturn().getResponse().getContentAsString();
		var tokenMatcher = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"").matcher(loggedIn);
		if (!tokenMatcher.find()) throw new AssertionError("Login response did not contain a JWT");

		mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + tokenMatcher.group(1)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.studentId").value(studentIdMatcher.group(1)))
			.andExpect(jsonPath("$.role").value("STUDENT"));
	}

	@Test
	void rejectsProtectedRequestsWithoutBearerTokenAsProblemDetail() throws Exception {
		mvc.perform(get("/api/v1/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
				.contentTypeCompatibleWith("application/problem+json"))
			.andExpect(jsonPath("$.title").value("Unauthorized"));
	}
}
