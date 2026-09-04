package com.marcosbarbero.deskspace.shared;

import java.util.List;

import com.marcosbarbero.deskspace.api.model.ApiProblem;
import com.marcosbarbero.deskspace.api.model.ApiProblemErrorsInner;
import com.marcosbarbero.deskspace.booking.DeskAlreadyBookedException;
import com.marcosbarbero.deskspace.booking.UnknownBookingException;
import com.marcosbarbero.deskspace.booking.UnknownDeskException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Every error response in this service is an ApiProblem, because that is what
 * api/openapi.yaml promises. A handler that returned anything else would be a contract
 * violation the browser client could not parse.
 */
@RestControllerAdvice
public class ProblemAdvice {

	@ExceptionHandler(DeskAlreadyBookedException.class)
	ResponseEntity<ApiProblem> alreadyBooked(DeskAlreadyBookedException ex) {
		return problem(HttpStatus.CONFLICT, "Desk already booked", ex.getMessage());
	}

	@ExceptionHandler({ UnknownDeskException.class, UnknownBookingException.class })
	ResponseEntity<ApiProblem> notFound(RuntimeException ex) {
		return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiProblem> illegal(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiProblem> invalidBody(MethodArgumentNotValidException ex) {
		ApiProblem problem = new ApiProblem("Invalid request", HttpStatus.BAD_REQUEST.value());
		problem.setDetail("The request body failed validation");
		List<ApiProblemErrorsInner> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(fe -> new ApiProblemErrorsInner(fe.getField(),
					fe.getDefaultMessage() == null ? "is invalid" : fe.getDefaultMessage()))
			.toList();
		problem.setErrors(errors);
		return ResponseEntity.badRequest().body(problem);
	}

	private ResponseEntity<ApiProblem> problem(HttpStatus status, String title, String detail) {
		ApiProblem problem = new ApiProblem(title, status.value());
		problem.setDetail(detail);
		return ResponseEntity.status(status).body(problem);
	}

}
