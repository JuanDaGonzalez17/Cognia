package com.cognia.api.adapter.in.security;

import com.cognia.api.application.port.out.TokenIssuerPort;
import com.cognia.api.domain.model.StudentAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService implements TokenIssuerPort {
	private final SecretKey key;
	private final long expirationSeconds;

	public JwtTokenService(@Value("${cognia.jwt.secret:}") String configuredSecret,
			@Value("${cognia.jwt.expiration-seconds:86400}") long expirationSeconds) {
		if (expirationSeconds < 60) throw new IllegalArgumentException("JWT expiration must be at least 60 seconds");
		this.key = configuredSecret.isBlank()
				? Jwts.SIG.HS256.key().build()
				: Keys.hmacShaKeyFor(Decoders.BASE64.decode(configuredSecret));
		this.expirationSeconds = expirationSeconds;
	}

	@Override
	public String issue(StudentAccount account) {
		var now = Instant.now();
		return Jwts.builder()
				.subject(account.id().toString())
				.claim("email", account.email())
				.claim("role", account.role().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(key)
				.compact();
	}

	public JwtIdentity parse(String token) {
		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		return new JwtIdentity(UUID.fromString(claims.getSubject()), claims.get("email", String.class),
				StudentAccount.Role.valueOf(claims.get("role", String.class)));
	}

	@Override
	public long expiresInSeconds() {
		return expirationSeconds;
	}

	public record JwtIdentity(UUID studentId, String email, StudentAccount.Role role) {}
}
