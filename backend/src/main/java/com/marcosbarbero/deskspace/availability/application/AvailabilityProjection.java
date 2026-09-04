package com.marcosbarbero.deskspace.availability.application;

import com.marcosbarbero.deskspace.availability.application.port.out.OccupiedDesks;
import com.marcosbarbero.deskspace.booking.domain.event.BookingCancelled;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;

import org.springframework.stereotype.Service;

/**
 * Keeps the read model current from the booking slice's published events.
 *
 * This class and the events it names are the entire coupling between booking and
 * availability. There is no call in either direction, which is what lets the architecture
 * rule say that availability may depend on {@code booking.domain.event} and on nothing
 * else in booking.
 *
 * Both handlers are idempotent: replaying an event produces the same projection, because
 * the day these arrive from a broker they will occasionally arrive twice.
 */
@Service
public class AvailabilityProjection {

	private final OccupiedDesks occupied;

	public AvailabilityProjection(OccupiedDesks occupied) {
		this.occupied = occupied;
	}

	public void on(DeskBooked event) {
		this.occupied.occupy(event.deskId(), event.date());
	}

	public void on(BookingCancelled event) {
		this.occupied.release(event.deskId(), event.date());
	}

}
