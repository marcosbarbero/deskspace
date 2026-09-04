package com.marcosbarbero.deskspace.booking;

import java.time.LocalDate;
import java.util.UUID;

public class DeskAlreadyBookedException extends RuntimeException {

	public DeskAlreadyBookedException(UUID deskId, LocalDate date) {
		super("Desk %s is already booked on %s".formatted(deskId, date));
	}

}
