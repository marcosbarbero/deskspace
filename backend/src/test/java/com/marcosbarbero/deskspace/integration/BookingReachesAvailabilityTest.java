package com.marcosbarbero.deskspace.integration;

import java.time.Clock;
import java.util.List;

import com.marcosbarbero.deskspace.availability.application.DesksOnDate;
import com.marcosbarbero.deskspace.availability.application.port.out.OccupiedDesks;
import com.marcosbarbero.deskspace.availability.domain.DeskAvailability;
import com.marcosbarbero.deskspace.booking.application.BookDesk;
import com.marcosbarbero.deskspace.booking.application.CancelBooking;
import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The seam, wired for real.
 *
 * Everything else in this suite tests one side of the event: that booking publishes, or
 * that the projection reacts. Both can be green while nothing is subscribed, which is a
 * failure mode that looks like nothing at all until a screen shows a desk that was taken
 * an hour ago.
 *
 * So this one boots the container, books through the real use case, and asks the read
 * side. It is the only test here that would notice a missing {@code @EventListener}.
 */
@SpringBootTest
@Import(BookingReachesAvailabilityTest.FixedClock.class)
class BookingReachesAvailabilityTest {

	@TestConfiguration
	static class FixedClock {

		@Bean
		@Primary
		Clock testClock() {
			return Fixtures.clockAt(Fixtures.TODAY);
		}

	}

	@Autowired
	private BookDesk bookDesk;

	@Autowired
	private CancelBooking cancelBooking;

	@Autowired
	private DesksOnDate desksOnDate;

	@Autowired
	private Bookings bookings;

	@Autowired
	private OccupiedDesks occupied;

	@BeforeEach
	void reset() {
		this.bookings.deleteAll();
		this.occupied.clear();
	}

	private boolean availabilityOf(java.util.UUID deskId) {
		List<DeskAvailability> desks = this.desksOnDate.on(Fixtures.TODAY);
		return desks.stream().filter((desk) -> desk.deskId().equals(deskId)).findFirst().orElseThrow().available();
	}

	@Test
	void a_desk_booked_on_the_write_side_stops_being_available_on_the_read_side() {
		assertThat(availabilityOf(Fixtures.A01)).isTrue();

		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(availabilityOf(Fixtures.A01)).isFalse();
	}

	@Test
	void booking_one_desk_leaves_the_others_alone() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(availabilityOf(Fixtures.A02)).isTrue();
	}

	@Test
	void cancelling_makes_the_desk_available_again() {
		Booking booking = this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		this.cancelBooking.cancel(booking.id());

		assertThat(availabilityOf(Fixtures.A01)).isTrue();
	}

	@Test
	void a_booking_for_another_date_does_not_affect_today() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY.plusDays(1), "ada@example.com");

		assertThat(availabilityOf(Fixtures.A01)).isTrue();
	}

}
