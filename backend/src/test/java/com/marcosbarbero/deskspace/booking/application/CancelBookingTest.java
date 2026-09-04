package com.marcosbarbero.deskspace.booking.application;

import com.marcosbarbero.deskspace.booking.adapter.out.persistence.InMemoryBookings;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.BookingStatus;
import com.marcosbarbero.deskspace.booking.domain.UnknownBookingException;
import com.marcosbarbero.deskspace.booking.domain.event.BookingCancelled;
import com.marcosbarbero.deskspace.support.Fixtures;
import com.marcosbarbero.deskspace.support.RecordingDomainEvents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelBookingTest {

	private InMemoryBookings bookings;

	private RecordingDomainEvents events;

	private CancelBooking cancelBooking;

	@BeforeEach
	void setUp() {
		this.bookings = new InMemoryBookings();
		this.events = new RecordingDomainEvents();
		this.cancelBooking = new CancelBooking(this.bookings, this.events, Fixtures.clockAt(Fixtures.TODAY));
	}

	private Booking existing() {
		return this.bookings
			.save(Booking.confirmed(Fixtures.countingIds().get(), Fixtures.A01, Fixtures.TODAY, "ada@example.com"));
	}

	@Test
	void cancels_a_confirmed_booking() {
		Booking booking = existing();

		this.cancelBooking.cancel(booking.id());

		assertThat(this.bookings.byId(booking.id()).orElseThrow().status()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	void announces_the_cancellation() {
		Booking booking = existing();

		this.cancelBooking.cancel(booking.id());

		assertThat(this.events.of(BookingCancelled.class)).singleElement().satisfies((event) -> {
			assertThat(event.bookingId()).isEqualTo(booking.id());
			assertThat(event.deskId()).isEqualTo(Fixtures.A01);
			assertThat(event.date()).isEqualTo(Fixtures.TODAY);
		});
	}

	@Test
	void cancelling_twice_succeeds_and_announces_once() {
		Booking booking = existing();

		this.cancelBooking.cancel(booking.id());
		this.cancelBooking.cancel(booking.id());

		assertThat(this.bookings.byId(booking.id()).orElseThrow().status()).isEqualTo(BookingStatus.CANCELLED);
		assertThat(this.events.of(BookingCancelled.class)).hasSize(1);
	}

	@Test
	void a_cancelled_desk_is_free_again() {
		Booking booking = existing();

		this.cancelBooking.cancel(booking.id());

		assertThat(this.bookings.isTaken(Fixtures.A01, Fixtures.TODAY)).isFalse();
	}

	@Test
	void refuses_an_unknown_booking() {
		assertThatThrownBy(() -> this.cancelBooking.cancel(Fixtures.UNKNOWN))
			.isInstanceOf(UnknownBookingException.class);

		assertThat(this.events.published()).isEmpty();
	}

}
