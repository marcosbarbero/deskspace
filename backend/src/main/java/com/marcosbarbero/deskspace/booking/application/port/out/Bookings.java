package com.marcosbarbero.deskspace.booking.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.marcosbarbero.deskspace.booking.domain.Booking;

/** Outbound port: where bookings are kept. */
public interface Bookings {

	Booking save(Booking booking);

	Optional<Booking> byId(UUID id);

	boolean isTaken(UUID deskId, LocalDate date);

	void deleteAll();

}
