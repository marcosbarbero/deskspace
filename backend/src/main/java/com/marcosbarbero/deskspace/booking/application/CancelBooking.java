package com.marcosbarbero.deskspace.booking.application;

import java.time.Clock;
import java.util.UUID;

import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.UnknownBookingException;
import com.marcosbarbero.deskspace.booking.domain.event.BookingCancelled;
import com.marcosbarbero.deskspace.shared.event.DomainEvents;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelBooking {

	private final Bookings bookings;

	private final DomainEvents events;

	private final Clock clock;

	public CancelBooking(Bookings bookings, DomainEvents events, Clock clock) {
		this.bookings = bookings;
		this.events = events;
		this.clock = clock;
	}

	/**
	 * Cancelling is idempotent: a retried request succeeds and changes nothing.
	 *
	 * The event is published only on the transition, not on every call. A subscriber that
	 * received two cancellations for one booking would have to decide what that meant,
	 * and there is no useful answer.
	 */
	@Transactional
	public void cancel(UUID bookingId) {
		Booking booking = this.bookings.byId(bookingId).orElseThrow(() -> new UnknownBookingException(bookingId));
		if (booking.isCancelled()) {
			return;
		}
		Booking cancelled = this.bookings.save(booking.cancelled());
		this.events
			.publish(new BookingCancelled(cancelled.id(), cancelled.deskId(), cancelled.date(), this.clock.instant()));
	}

}
