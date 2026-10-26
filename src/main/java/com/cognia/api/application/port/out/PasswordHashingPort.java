package com.cognia.api.application.port.out;

public interface PasswordHashingPort {
	String hash(String rawPassword);
	boolean matches(String rawPassword, String passwordHash);
}
