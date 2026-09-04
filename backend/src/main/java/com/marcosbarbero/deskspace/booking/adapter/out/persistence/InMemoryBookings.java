package com.marcosbarbero.deskspace.booking.adapter.out.persistence;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.BookingStatus;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The default adapter: no database, no Docker, a fresh clone goes green.
 *
 * Enabled unless the postgres profile is active. See ADR 0005 and ADR 0010.
 */
@Component
@Profile("!postgres")
public class InMemoryBookings implements Bookings {

	private final Map<UUID, Booking> bookings = new ConcurrentHashMap<>();

	@Override
	public Booking save(Booking booking) {
		this.bookings.put(booking.id(), booking);
		return booking;
	}

	@Override
	public Optional<Booking> byId(UUID id) {
		return Optional.ofNullable(this.bookings.get(id));
	}

	@Override
	public boolean isTaken(UUID deskId, LocalDate date) {
		return this.bookings.values()
			.stream()
			.filter((booking) -> booking.status() == BookingStatus.CONFIRMED)
			.anyMatch((booking) -> booking.deskId().equals(deskId) && booking.date().equals(date));
	}

	@Override
	public void deleteAll() {
		this.bookings.clear();
	}

}
