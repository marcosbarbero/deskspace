package com.marcosbarbero.deskspace.booking.domain;

import java.time.LocalDate;
import java.util.UUID;

public record Booking(UUID id, UUID deskId, LocalDate date, String bookedBy, BookingStatus status) {

	public static Booking confirmed(UUID id, UUID deskId, LocalDate date, String bookedBy) {
		return new Booking(id, deskId, date, bookedBy, BookingStatus.CONFIRMED);
	}

	public boolean isCancelled() {
		return this.status == BookingStatus.CANCELLED;
	}

	public Booking cancelled() {
		return new Booking(this.id, this.deskId, this.date, this.bookedBy, BookingStatus.CANCELLED);
	}
}
