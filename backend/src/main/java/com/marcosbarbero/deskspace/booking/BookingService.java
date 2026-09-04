package com.marcosbarbero.deskspace.booking;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

import com.marcosbarbero.deskspace.desk.DeskCatalog;

import org.springframework.stereotype.Service;

/**
 * Booking rules. The clock and the id source are injected rather than called
 * statically, so every rule here is testable without sleeping or guessing:
 * see docs/adr/0002-deterministic-time-in-tests.md.
 */
@Service
public class BookingService {

	private final BookingRepository repository;

	private final DeskCatalog desks;

	private final Clock clock;

	private final Supplier<UUID> ids;

	public BookingService(BookingRepository repository, DeskCatalog desks, Clock clock) {
		this(repository, desks, clock, UUID::randomUUID);
	}

	BookingService(BookingRepository repository, DeskCatalog desks, Clock clock, Supplier<UUID> ids) {
		this.repository = repository;
		this.desks = desks;
		this.clock = clock;
		this.ids = ids;
	}

	public Booking book(UUID deskId, LocalDate date, String bookedBy) {
		if (!desks.exists(deskId)) {
			throw new UnknownDeskException(deskId);
		}
		if (date.isBefore(LocalDate.now(clock))) {
			throw new IllegalArgumentException("A desk cannot be booked for a date in the past");
		}
		if (repository.isTaken(deskId, date)) {
			throw new DeskAlreadyBookedException(deskId, date);
		}
		return repository.save(new Booking(ids.get(), deskId, date, bookedBy, BookingStatus.CONFIRMED));
	}

	/**
	 * Cancelling is idempotent: cancelling an already cancelled booking succeeds
	 * and changes nothing, so a retried request is not an error.
	 */
	public void cancel(UUID bookingId) {
		Booking booking = repository.byId(bookingId).orElseThrow(() -> new UnknownBookingException(bookingId));
		repository.save(booking.cancelled());
	}
}
