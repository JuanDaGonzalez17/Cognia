package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.StudentAccount;

public interface TokenIssuerPort {
	String issue(StudentAccount account);
	long expiresInSeconds();
}
