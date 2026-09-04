package com.marcosbarbero.deskspace.booking.adapter.in.web;

import com.marcosbarbero.deskspace.api.model.ApiProblem;
import com.marcosbarbero.deskspace.booking.domain.BookingDateInThePastException;
import com.marcosbarbero.deskspace.booking.domain.DeskAlreadyBookedException;
import com.marcosbarbero.deskspace.booking.domain.UnknownBookingException;
import com.marcosbarbero.deskspace.booking.domain.UnknownDeskException;
import com.marcosbarbero.deskspace.shared.adapter.in.web.Problems;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Booking translates its own failures.
 *
 * A single shared advice class importing every slice's exceptions is the obvious
 * arrangement and it creates a cycle: shared would depend on booking while booking
 * depends on shared for the event port. ArchitectureRulesTest found exactly that, which
 * is why this class exists.
 *
 * The status codes are not incidental. The 409 is what lets a screen tell a member the
 * desk went while they were reading, and pacts/ pins it.
 */
@RestControllerAdvice(assignableTypes = BookingController.class)
public class BookingProblemAdvice {

	@ExceptionHandler(DeskAlreadyBookedException.class)
	ResponseEntity<ApiProblem> alreadyBooked(DeskAlreadyBookedException ex) {
		return Problems.of(HttpStatus.CONFLICT, "Desk already booked", ex.getMessage());
	}

	@ExceptionHandler({ UnknownDeskException.class, UnknownBookingException.class })
	ResponseEntity<ApiProblem> notFound(RuntimeException ex) {
		return Problems.of(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
	}

	@ExceptionHandler(BookingDateInThePastException.class)
	ResponseEntity<ApiProblem> inThePast(BookingDateInThePastException ex) {
		return Problems.of(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
	}

}
