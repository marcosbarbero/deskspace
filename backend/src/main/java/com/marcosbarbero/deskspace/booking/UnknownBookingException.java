package com.marcosbarbero.deskspace.booking;

import java.util.UUID;

public class UnknownBookingException extends RuntimeException {

	public UnknownBookingException(UUID bookingId) {
		super("No booking with id %s".formatted(bookingId));
	}

}
