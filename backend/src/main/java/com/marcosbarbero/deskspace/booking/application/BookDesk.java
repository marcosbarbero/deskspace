package com.marcosbarbero.deskspace.booking.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.booking.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.BookingDateInThePastException;
import com.marcosbarbero.deskspace.booking.domain.DeskAlreadyBookedException;
import com.marcosbarbero.deskspace.booking.domain.UnknownDeskException;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;
import com.marcosbarbero.deskspace.shared.event.DomainEvents;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Taking a desk for a date.
 *
 * Everything this class touches is an interface it declared itself, plus its own domain.
 * That is what makes it testable with four hand-written fakes and no container, and it is
 * what stops a booking rule quietly acquiring a dependency on how desks are stored.
 */
@Service
public class BookDesk {

	private final Bookings bookings;

	private final DeskDirectory desks;

	private final DomainEvents events;

	private final Clock clock;

	private final Supplier<UUID> ids;

	public BookDesk(Bookings bookings, DeskDirectory desks, DomainEvents events, Clock clock, Supplier<UUID> ids) {
		this.bookings = bookings;
		this.desks = desks;
		this.events = events;
		this.clock = clock;
		this.ids = ids;
	}

	@Transactional
	public Booking book(UUID deskId, LocalDate date, String bookedBy) {
		if (!this.desks.exists(deskId)) {
			throw new UnknownDeskException(deskId);
		}
		LocalDate today = LocalDate.now(this.clock);
		if (date.isBefore(today)) {
			throw new BookingDateInThePastException(date, today);
		}
		if (this.bookings.isTaken(deskId, date)) {
			throw new DeskAlreadyBookedException(deskId, date);
		}

		Booking booking = this.bookings.save(Booking.confirmed(this.ids.get(), deskId, date, bookedBy));
		this.events.publish(new DeskBooked(booking.id(), booking.deskId(), booking.date(), this.clock.instant()));
		return booking;
	}

}
