package com.marcosbarbero.deskspace.availability.application;

import java.time.Instant;

import com.marcosbarbero.deskspace.availability.adapter.out.persistence.InMemoryOccupiedDesks;
import com.marcosbarbero.deskspace.booking.domain.event.BookingCancelled;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The read model, driven by events and nothing else.
 *
 * Both handlers are asserted to be idempotent. In process an event arrives once, so this
 * looks like belt and braces; the day these come from a broker, at-least once delivery
 * makes it the property the projection lives or dies on, and the test that proves it
 * should already exist by then.
 */
class AvailabilityProjectionTest {

	private InMemoryOccupiedDesks occupied;

	private AvailabilityProjection projection;

	@BeforeEach
	void setUp() {
		this.occupied = new InMemoryOccupiedDesks();
		this.projection = new AvailabilityProjection(this.occupied);
	}

	private DeskBooked booked() {
		return new DeskBooked(Fixtures.UNKNOWN, Fixtures.A01, Fixtures.TODAY, Instant.EPOCH);
	}

	private BookingCancelled cancelled() {
		return new BookingCancelled(Fixtures.UNKNOWN, Fixtures.A01, Fixtures.TODAY, Instant.EPOCH);
	}

	@Test
	void a_booking_occupies_the_desk_on_its_date() {
		this.projection.on(booked());

		assertThat(this.occupied.on(Fixtures.TODAY)).containsExactly(Fixtures.A01);
	}

	@Test
	void a_booking_does_not_occupy_another_date() {
		this.projection.on(booked());

		assertThat(this.occupied.on(Fixtures.TODAY.plusDays(1))).isEmpty();
	}

	@Test
	void a_cancellation_releases_the_desk() {
		this.projection.on(booked());

		this.projection.on(cancelled());

		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
	}

	@Test
	void the_same_booking_event_twice_changes_nothing() {
		this.projection.on(booked());
		this.projection.on(booked());

		assertThat(this.occupied.on(Fixtures.TODAY)).containsExactly(Fixtures.A01);
	}

	@Test
	void the_same_cancellation_twice_changes_nothing() {
		this.projection.on(booked());
		this.projection.on(cancelled());
		this.projection.on(cancelled());

		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
	}

	@Test
	void a_cancellation_for_something_never_booked_is_harmless() {
		this.projection.on(cancelled());

		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
	}

}
