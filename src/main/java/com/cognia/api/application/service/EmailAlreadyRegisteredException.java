package com.cognia.api.application.service;

public class EmailAlreadyRegisteredException extends RuntimeException {
	public EmailAlreadyRegisteredException() {
		super("An account with this email already exists");
	}
}
