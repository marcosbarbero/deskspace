package com.marcosbarbero.deskspace.shared.adapter.in.web;

import com.marcosbarbero.deskspace.api.model.ApiProblem;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Builds the one error shape api/openapi.yaml promises.
 *
 * Shared because every slice returns the same shape, and it knows about no slice in
 * return, which is what keeps the dependency one-way.
 */
public final class Problems {

	private Problems() {
	}

	public static ResponseEntity<ApiProblem> of(HttpStatus status, String title, String detail) {
		ApiProblem problem = new ApiProblem(title, status.value());
		problem.setDetail(detail);
		return ResponseEntity.status(status).body(problem);
	}

}
