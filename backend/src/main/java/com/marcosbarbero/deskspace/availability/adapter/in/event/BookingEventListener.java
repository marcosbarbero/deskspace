package com.marcosbarbero.deskspace.availability.adapter.in.event;

import com.marcosbarbero.deskspace.availability.application.AvailabilityProjection;
import com.marcosbarbero.deskspace.booking.domain.event.BookingCancelled;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter for events, exactly as a controller is one for HTTP.
 *
 * The transport annotation lives here and nowhere else, so {@link AvailabilityProjection}
 * is a plain object a test can call directly, and moving from an in-process publisher to
 * a queue replaces this class alone.
 */
@Component
public class BookingEventListener {

	private final AvailabilityProjection projection;

	public BookingEventListener(AvailabilityProjection projection) {
		this.projection = projection;
	}

	@EventListener
	void on(DeskBooked event) {
		this.projection.on(event);
	}

	@EventListener
	void on(BookingCancelled event) {
		this.projection.on(event);
	}

}
