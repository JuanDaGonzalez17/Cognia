package com.cognia.api.adapter.in.web;

import com.cognia.api.adapter.in.security.CurrentStudent;
import com.cognia.api.application.port.in.AuthUseCase;
import com.cognia.api.domain.model.StudentAccount;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthUseCase auth;

	public AuthController(AuthUseCase auth) {
		this.auth = auth;
	}

	@PostMapping("/register")
	@Operation(security = {})
	public ResponseEntity<RegisteredAccountResponse> register(@Valid @RequestBody RegisterRequest request) {
		var account = auth.register(request.email(), request.password(), request.fullName());
		return ResponseEntity.status(HttpStatus.CREATED).body(RegisteredAccountResponse.from(account));
	}

	@PostMapping("/login")
	@Operation(security = {})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		var result = auth.login(request.email(), request.password());
		return new LoginResponse(result.token(), result.expiresIn(), result.studentId(), result.fullName());
	}

	@GetMapping("/me")
	public ProfileResponse me(@CurrentStudent UUID studentId) {
		return ProfileResponse.from(auth.getProfile(studentId));
	}

	public record RegisterRequest(@NotBlank @Email @Size(max = 150) String email,
			@NotBlank @Size(min = 8, max = 72) String password,
			@NotBlank @Size(max = 120) String fullName) {}
	public record LoginRequest(@NotBlank @Email @Size(max = 150) String email, @NotBlank String password) {}
	public record RegisteredAccountResponse(UUID studentId, String email, String fullName) {
		static RegisteredAccountResponse from(StudentAccount account) {
			return new RegisteredAccountResponse(account.id(), account.email(), account.fullName());
		}
	}
	public record LoginResponse(String token, long expiresIn, UUID studentId, String fullName) {}
	public record ProfileResponse(UUID studentId, String email, String fullName, String role) {
		static ProfileResponse from(StudentAccount account) {
			return new ProfileResponse(account.id(), account.email(), account.fullName(), account.role().name());
		}
	}
}
