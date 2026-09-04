package com.marcosbarbero.deskspace.shared.adapter.in.web;

import java.util.List;

import com.marcosbarbero.deskspace.api.model.ApiProblem;
import com.marcosbarbero.deskspace.api.model.ApiProblemErrorsInner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Failures that belong to no slice: a body that did not satisfy the schema the spec
 * declares. Everything domain-specific is translated by the slice that raised it, which
 * is what stops this class importing every slice in the service and creating a cycle.
 */
@RestControllerAdvice
public class ValidationProblemAdvice {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiProblem> invalidBody(MethodArgumentNotValidException ex) {
		ResponseEntity<ApiProblem> response = Problems.of(HttpStatus.BAD_REQUEST, "Invalid request",
				"The request body failed validation");
		List<ApiProblemErrorsInner> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map((error) -> new ApiProblemErrorsInner(error.getField(),
					error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage()))
			.toList();
		ApiProblem problem = response.getBody();
		if (problem != null) {
			problem.setErrors(errors);
		}
		return response;
	}

}
