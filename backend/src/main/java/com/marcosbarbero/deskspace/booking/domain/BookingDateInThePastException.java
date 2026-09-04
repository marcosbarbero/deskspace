package com.marcosbarbero.deskspace.booking.domain;

import java.time.LocalDate;

public class BookingDateInThePastException extends RuntimeException {

	public BookingDateInThePastException(LocalDate date, LocalDate today) {
		super("A desk cannot be booked for %s, which is in the past; today is %s".formatted(date, today));
	}

}
