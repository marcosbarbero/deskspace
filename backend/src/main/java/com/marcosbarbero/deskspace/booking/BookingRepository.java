package com.marcosbarbero.deskspace.booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {

	private final Map<UUID, Booking> bookings = new ConcurrentHashMap<>();

	public Booking save(Booking booking) {
		bookings.put(booking.id(), booking);
		return booking;
	}

	public Optional<Booking> byId(UUID id) {
		return Optional.ofNullable(bookings.get(id));
	}

	public List<Booking> confirmedOn(LocalDate date) {
		return bookings.values().stream()
				.filter(b -> b.status() == BookingStatus.CONFIRMED)
				.filter(b -> b.date().equals(date))
				.toList();
	}

	public boolean isTaken(UUID deskId, LocalDate date) {
		return confirmedOn(date).stream().anyMatch(b -> b.deskId().equals(deskId));
	}
}
