package com.marcosbarbero.deskspace.booking;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRepositoryTest {

	private static final LocalDate DATE = LocalDate.of(2026, 3, 2);

	private static final UUID DESK = UUID.fromString("11111111-0000-0000-0000-000000000001");

	private static final UUID OTHER_DESK = UUID.fromString("11111111-0000-0000-0000-000000000002");

	private final BookingRepository repository = new BookingRepository();

	private Booking confirmed(UUID deskId, LocalDate date) {
		return new Booking(UUID.randomUUID(), deskId, date, "ada@example.com", BookingStatus.CONFIRMED);
	}

	@Test
	void a_desk_with_no_booking_is_free() {
		assertThat(repository.isTaken(DESK, DATE)).isFalse();
	}

	@Test
	void a_desk_with_a_confirmed_booking_is_taken() {
		repository.save(confirmed(DESK, DATE));

		assertThat(repository.isTaken(DESK, DATE)).isTrue();
	}

	@Test
	void a_booking_on_one_desk_does_not_take_another() {
		repository.save(confirmed(DESK, DATE));

		assertThat(repository.isTaken(OTHER_DESK, DATE)).isFalse();
	}

	@Test
	void a_booking_on_one_date_does_not_take_another_date() {
		repository.save(confirmed(DESK, DATE));

		assertThat(repository.isTaken(DESK, DATE.plusDays(1))).isFalse();
	}

	@Test
	void confirmed_bookings_are_listed_for_their_date_only() {
		repository.save(confirmed(DESK, DATE));
		repository.save(confirmed(OTHER_DESK, DATE.plusDays(1)));

		assertThat(repository.confirmedOn(DATE)).hasSize(1).allMatch(b -> b.deskId().equals(DESK));
	}

	@Test
	void deleting_everything_leaves_nothing_booked() {
		repository.save(confirmed(DESK, DATE));

		repository.deleteAll();

		assertThat(repository.confirmedOn(DATE)).isEmpty();
		assertThat(repository.isTaken(DESK, DATE)).isFalse();
	}

}
