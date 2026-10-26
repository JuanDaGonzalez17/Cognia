package com.cognia.api.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record StudentAccount(UUID id, String email, String passwordHash, String fullName, Role role, boolean active) {
	public StudentAccount {
		Objects.requireNonNull(id, "Student id must not be null");
		if (email == null || email.isBlank()) throw new IllegalArgumentException("Email must not be blank");
		if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password hash must not be blank");
		if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Full name must not be blank");
		Objects.requireNonNull(role, "Student role must not be null");
		email = email.trim().toLowerCase(Locale.ROOT);
		fullName = fullName.trim();
	}

	public enum Role { STUDENT }
}
