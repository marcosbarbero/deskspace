package com.marcosbarbero.deskspace.booking.adapter.out.persistence;

import java.util.UUID;

import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.BookingStatus;
import com.marcosbarbero.deskspace.booking.domain.DeskAlreadyBookedException;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The persistence adapter against a real Postgres.
 *
 * Tagged and excluded from the default build, because a fresh clone should go green
 * without Docker. Run with -Pdatabase.
 *
 * The rule this exists for is the second test. Everything else here would pass against a
 * map.
 */
@Tag("database")
@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres")
class JdbcBookingsTest {

	@Container
	@SuppressWarnings("resource")
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	private Bookings bookings;

	@BeforeEach
	void clean() {
		this.bookings.deleteAll();
	}

	private Booking confirmed(UUID id) {
		return Booking.confirmed(id, Fixtures.A01, Fixtures.TODAY, "ada@example.com");
	}

	@Test
	void the_adapter_in_use_is_the_database_one() {
		assertThat(this.bookings).isInstanceOf(JdbcBookings.class);
	}

	@Test
	void a_booking_is_readable_after_being_written() {
		Booking saved = this.bookings.save(confirmed(Fixtures.UNKNOWN));

		assertThat(this.bookings.byId(saved.id())).contains(saved);
	}

	@Test
	void the_database_refuses_a_second_confirmed_booking_for_the_same_desk_and_day() {
		this.bookings.save(confirmed(UUID.randomUUID()));

		assertThatThrownBy(() -> this.bookings.save(confirmed(UUID.randomUUID())))
			.isInstanceOf(DeskAlreadyBookedException.class);
	}

	@Test
	void a_cancelled_booking_does_not_hold_the_desk() {
		Booking booking = this.bookings.save(confirmed(UUID.randomUUID()));

		this.bookings.save(booking.cancelled());

		assertThat(this.bookings.isTaken(Fixtures.A01, Fixtures.TODAY)).isFalse();
	}

	@Test
	void the_same_desk_can_be_booked_again_after_a_cancellation() {
		Booking first = this.bookings.save(confirmed(UUID.randomUUID()));
		this.bookings.save(first.cancelled());

		assertThat(this.bookings.save(confirmed(UUID.randomUUID())).status()).isEqualTo(BookingStatus.CONFIRMED);
	}

	@Test
	void a_desk_booked_on_one_day_is_free_on_another() {
		this.bookings.save(confirmed(UUID.randomUUID()));

		assertThat(this.bookings.isTaken(Fixtures.A01, Fixtures.TODAY.plusDays(1))).isFalse();
	}

	@Test
	void an_unknown_id_reads_back_as_absent() {
		assertThat(this.bookings.byId(UUID.randomUUID())).isEmpty();
	}

}
