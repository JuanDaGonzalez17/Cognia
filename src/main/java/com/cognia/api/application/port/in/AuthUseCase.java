package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.StudentAccount;
import java.util.UUID;

public interface AuthUseCase {
	StudentAccount register(String email, String rawPassword, String fullName);
	AuthResult login(String email, String rawPassword);
	StudentAccount getProfile(UUID studentId);

	record AuthResult(String token, long expiresIn, UUID studentId, String fullName) {}
}
