package com.cognia.api.application.service;

import com.cognia.api.application.port.in.AuthUseCase;
import com.cognia.api.application.port.in.AuthUseCase.AuthResult;
import com.cognia.api.application.port.out.PasswordHashingPort;
import com.cognia.api.application.port.out.StudentAccountRepository;
import com.cognia.api.application.port.out.TokenIssuerPort;
import com.cognia.api.domain.model.StudentAccount;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService implements AuthUseCase {
	private final StudentAccountRepository accounts;
	private final PasswordHashingPort passwords;
	private final TokenIssuerPort tokens;

	public AuthService(StudentAccountRepository accounts, PasswordHashingPort passwords, TokenIssuerPort tokens) {
		this.accounts = accounts;
		this.passwords = passwords;
		this.tokens = tokens;
	}

	@Override
	public StudentAccount register(String email, String rawPassword, String fullName) {
		var normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		if (accounts.existsByEmail(normalizedEmail)) throw new EmailAlreadyRegisteredException();
		return accounts.save(new StudentAccount(UUID.randomUUID(), normalizedEmail, passwords.hash(rawPassword), fullName,
				StudentAccount.Role.STUDENT, true));
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResult login(String email, String rawPassword) {
		var account = accounts.findByEmail(email.trim().toLowerCase(Locale.ROOT))
				.filter(StudentAccount::active)
				.filter(candidate -> passwords.matches(rawPassword, candidate.passwordHash()))
				.orElseThrow(InvalidCredentialsException::new);
		return new AuthResult(tokens.issue(account), tokens.expiresInSeconds(), account.id(), account.fullName());
	}

	@Override
	@Transactional(readOnly = true)
	public StudentAccount getProfile(UUID studentId) {
		return accounts.findById(studentId).filter(StudentAccount::active)
				.orElseThrow(() -> new ResourceNotFoundException("Student account was not found"));
	}

}
