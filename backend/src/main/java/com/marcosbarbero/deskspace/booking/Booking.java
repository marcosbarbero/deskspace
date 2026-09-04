package com.marcosbarbero.deskspace.booking;

import java.time.LocalDate;
import java.util.UUID;

public record Booking(UUID id, UUID deskId, LocalDate date, String bookedBy, BookingStatus status) {

	public Booking cancelled() {
		return new Booking(id, deskId, date, bookedBy, BookingStatus.CANCELLED);
	}
}
