package com.marcosbarbero.deskspace.booking.application;

import java.util.Set;
import java.util.UUID;

import com.marcosbarbero.deskspace.booking.adapter.out.persistence.InMemoryBookings;
import com.marcosbarbero.deskspace.booking.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.BookingDateInThePastException;
import com.marcosbarbero.deskspace.booking.domain.BookingStatus;
import com.marcosbarbero.deskspace.booking.domain.DeskAlreadyBookedException;
import com.marcosbarbero.deskspace.booking.domain.UnknownDeskException;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;
import com.marcosbarbero.deskspace.support.Fixtures;
import com.marcosbarbero.deskspace.support.RecordingDomainEvents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The booking rules, with no Spring context and no HTTP. Every collaborator is an
 * interface this use case declared, so the whole setup is four lines.
 */
class BookDeskTest {

	private static final Set<UUID> KNOWN_DESKS = Set.of(Fixtures.A01, Fixtures.A02);

	private InMemoryBookings bookings;

	private RecordingDomainEvents events;

	private BookDesk bookDesk;

	@BeforeEach
	void setUp() {
		this.bookings = new InMemoryBookings();
		this.events = new RecordingDomainEvents();
		DeskDirectory desks = KNOWN_DESKS::contains;
		this.bookDesk = new BookDesk(this.bookings, desks, this.events, Fixtures.clockAt(Fixtures.TODAY),
				Fixtures.countingIds());
	}

	@Test
	void books_a_free_desk() {
		Booking booking = this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(booking.deskId()).isEqualTo(Fixtures.A01);
		assertThat(booking.bookedBy()).isEqualTo("ada@example.com");
	}

	@Test
	void announces_that_the_desk_was_taken() {
		Booking booking = this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(this.events.of(DeskBooked.class)).singleElement().satisfies((event) -> {
			assertThat(event.bookingId()).isEqualTo(booking.id());
			assertThat(event.deskId()).isEqualTo(Fixtures.A01);
			assertThat(event.date()).isEqualTo(Fixtures.TODAY);
			assertThat(event.occurredAt()).isNotNull();
		});
	}

	@Test
	void today_is_bookable_because_the_boundary_is_inclusive() {
		assertThat(this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com")).isNotNull();
	}

	@Test
	void refuses_a_date_in_the_past() {
		assertThatThrownBy(() -> this.bookDesk.book(Fixtures.A01, Fixtures.TODAY.minusDays(1), "ada@example.com"))
			.isInstanceOf(BookingDateInThePastException.class)
			.hasMessageContaining("in the past");
	}

	@Test
	void refuses_a_desk_that_does_not_exist() {
		assertThatThrownBy(() -> this.bookDesk.book(Fixtures.UNKNOWN, Fixtures.TODAY, "ada@example.com"))
			.isInstanceOf(UnknownDeskException.class);
	}

	@Test
	void refuses_a_desk_already_booked_on_that_date() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThatThrownBy(() -> this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "grace@example.com"))
			.isInstanceOf(DeskAlreadyBookedException.class);
	}

	@Test
	void announces_nothing_when_it_refuses() {
		assertThatThrownBy(() -> this.bookDesk.book(Fixtures.UNKNOWN, Fixtures.TODAY, "ada@example.com"))
			.isInstanceOf(UnknownDeskException.class);

		assertThat(this.events.published()).isEmpty();
	}

	@Test
	void the_same_desk_is_free_on_a_different_date() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(this.bookDesk.book(Fixtures.A01, Fixtures.TODAY.plusDays(1), "grace@example.com")).isNotNull();
	}

	@Test
	void a_different_desk_is_free_on_the_same_date() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(this.bookDesk.book(Fixtures.A02, Fixtures.TODAY, "grace@example.com")).isNotNull();
	}

}
