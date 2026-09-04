package com.marcosbarbero.deskspace.booking;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.marcosbarbero.deskspace.desk.DeskCatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Every date here is fixed and every id is predictable, so a failure means the
 * rule broke rather than that the suite ran across midnight.
 */
class BookingServiceTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 3, 2);

	private static final UUID DESK = UUID.fromString("11111111-0000-0000-0000-000000000001");

	private BookingRepository repository;

	private BookingService service;

	@BeforeEach
	void setUp() {
		repository = new BookingRepository();
		AtomicInteger counter = new AtomicInteger();
		Clock fixed = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
		service = new BookingService(repository, new DeskCatalog(), fixed,
				() -> UUID.fromString("22222222-0000-0000-0000-%012d".formatted(counter.incrementAndGet())));
	}

	@Test
	void books_a_free_desk() {
		Booking booking = service.book(DESK, TODAY, "ada@example.com");

		assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(booking.deskId()).isEqualTo(DESK);
		assertThat(booking.bookedBy()).isEqualTo("ada@example.com");
		assertThat(repository.isTaken(DESK, TODAY)).isTrue();
	}

	@Test
	void today_is_bookable_because_the_boundary_is_inclusive() {
		assertThat(service.book(DESK, TODAY, "ada@example.com")).isNotNull();
	}

	@Test
	void refuses_a_date_in_the_past() {
		assertThatThrownBy(() -> service.book(DESK, TODAY.minusDays(1), "ada@example.com"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("past");
	}

	@Test
	void refuses_a_desk_that_does_not_exist() {
		UUID unknown = UUID.fromString("99999999-0000-0000-0000-000000000000");

		assertThatThrownBy(() -> service.book(unknown, TODAY, "ada@example.com"))
				.isInstanceOf(UnknownDeskException.class);
	}

	@Test
	void refuses_a_desk_already_booked_on_that_date() {
		service.book(DESK, TODAY, "ada@example.com");

		assertThatThrownBy(() -> service.book(DESK, TODAY, "grace@example.com"))
				.isInstanceOf(DeskAlreadyBookedException.class);
	}

	@Test
	void the_same_desk_is_free_on_a_different_date() {
		service.book(DESK, TODAY, "ada@example.com");

		assertThat(service.book(DESK, TODAY.plusDays(1), "grace@example.com")).isNotNull();
	}

	@Test
	void cancelling_frees_the_desk() {
		Booking booking = service.book(DESK, TODAY, "ada@example.com");

		service.cancel(booking.id());

		assertThat(repository.isTaken(DESK, TODAY)).isFalse();
		assertThat(repository.byId(booking.id()).orElseThrow().status()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	void cancelling_twice_is_not_an_error() {
		Booking booking = service.book(DESK, TODAY, "ada@example.com");
		service.cancel(booking.id());

		service.cancel(booking.id());

		assertThat(repository.byId(booking.id()).orElseThrow().status()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	void cancelling_an_unknown_booking_is_an_error() {
		assertThatThrownBy(() -> service.cancel(UUID.fromString("99999999-0000-0000-0000-000000000000")))
				.isInstanceOf(UnknownBookingException.class);
	}

	@Test
	void a_cancelled_booking_does_not_hide_a_desk_from_availability() {
		Booking booking = service.book(DESK, TODAY, "ada@example.com");
		service.cancel(booking.id());

		List<Booking> confirmed = repository.confirmedOn(TODAY);

		assertThat(confirmed).isEmpty();
	}
}
