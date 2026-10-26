package com.cognia.api.adapter.in.web;

import com.cognia.api.application.service.ResourceNotFoundException;
import com.cognia.api.application.service.InvalidPracticeRequestException;
import com.cognia.api.application.service.DiagnosticConflictException;
import com.cognia.api.application.service.EmailAlreadyRegisteredException;
import com.cognia.api.application.service.InvalidCredentialsException;
import com.cognia.api.domain.model.InvalidPrerequisiteException;
import com.cognia.api.domain.model.InvalidPrerequisiteException.Reason;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingRequestHeaderException;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
	}

	@ExceptionHandler(InvalidPrerequisiteException.class)
	public ResponseEntity<ProblemDetail> handleInvalidPrerequisite(InvalidPrerequisiteException exception) {
		var status = exception.reason() == Reason.INVALID_REQUEST
				? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.CONFLICT;
		return problem(status, "Invalid prerequisite relationship", exception.getMessage());
	}

	@ExceptionHandler(InvalidPracticeRequestException.class)
	public ResponseEntity<ProblemDetail> handleInvalidPractice(InvalidPracticeRequestException exception) {
		return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid practice request", exception.getMessage());
	}
	@ExceptionHandler(DiagnosticConflictException.class)
	public ResponseEntity<ProblemDetail> handleDiagnosticConflict(DiagnosticConflictException exception) {
		return problem(HttpStatus.CONFLICT, "Diagnostic conflict", exception.getMessage());
	}

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ResponseEntity<ProblemDetail> handleDuplicateEmail(EmailAlreadyRegisteredException exception) {
		return problem(HttpStatus.CONFLICT, "Email already registered", exception.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception) {
		return problem(HttpStatus.UNAUTHORIZED, "Invalid credentials", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleInvalidBody(MethodArgumentNotValidException exception) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "The request body does not satisfy the required constraints");
	}

	@ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingRequestHeaderException.class})
	public ResponseEntity<ProblemDetail> handleBadRequest(Exception exception) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", "A required UUID or header is missing or invalid");
	}

	private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
		var body = ProblemDetail.forStatusAndDetail(status, detail);
		body.setTitle(title);
		return ResponseEntity.status(status).body(body);
	}
}
