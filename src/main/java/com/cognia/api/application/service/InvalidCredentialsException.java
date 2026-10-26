package com.cognia.api.application.service;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException() {
		super("Email or password is invalid");
	}
}
