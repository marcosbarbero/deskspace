package com.marcosbarbero.deskspace.integration;

import java.time.Clock;
import java.util.UUID;

import com.marcosbarbero.deskspace.availability.application.DesksOnDate;
import com.marcosbarbero.deskspace.availability.application.port.out.OccupiedDesks;
import com.marcosbarbero.deskspace.availability.domain.DeskAvailability;
import com.marcosbarbero.deskspace.booking.application.BookDesk;
import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.shared.event.outbox.Outbox;
import com.marcosbarbero.deskspace.shared.event.outbox.OutboxRelay;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The property the outbox exists for, stated as a test.
 *
 * A booking commits and the event is delivered afterwards. If the process dies in
 * between, the booking is real and nobody ever tells the read model, and nothing retries.
 * The scheduled relay is replaced by a mock here so the gap is a place in the test rather
 * than a race, and the assertions are about what is true inside it.
 */
@Tag("database")
@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres")
@Import(OutboxSurvivesACrashTest.FixedClock.class)
class OutboxSurvivesACrashTest {

	@Container
	@SuppressWarnings("resource")
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@TestConfiguration
	static class FixedClock {

		@Bean
		@Primary
		Clock testClock() {
			return Fixtures.clockAt(Fixtures.TODAY);
		}

	}

	/** Nothing delivers unless this test says so. */
	@MockitoBean
	private com.marcosbarbero.deskspace.shared.event.outbox.ScheduledOutboxRelay scheduled;

	@Autowired
	private BookDesk bookDesk;

	@Autowired
	private OutboxRelay relay;

	@Autowired
	private Outbox outbox;

	@Autowired
	private Bookings bookings;

	@Autowired
	private OccupiedDesks occupied;

	@Autowired
	private DesksOnDate desksOnDate;

	@BeforeEach
	void clean() {
		this.bookings.deleteAll();
		this.occupied.clear();
		while (this.relay.deliver() > 0) {
			// drain anything a previous test left behind
		}
	}

	private boolean available(UUID deskId) {
		return this.desksOnDate.on(Fixtures.TODAY, null)
			.stream()
			.filter((desk) -> desk.deskId().equals(deskId))
			.findFirst()
			.map(DeskAvailability::available)
			.orElseThrow();
	}

	@Test
	void the_event_is_durable_before_anybody_has_delivered_it() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(this.outbox.unpublished(10)).hasSize(1);
		assertThat(available(Fixtures.A01)).isTrue();
	}

	@Test
	void the_relay_closes_the_gap() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		assertThat(this.relay.deliver()).isEqualTo(1);

		assertThat(available(Fixtures.A01)).isFalse();
		assertThat(this.outbox.unpublished(10)).isEmpty();
	}

	@Test
	void delivering_twice_leaves_the_read_model_alone() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");
		this.relay.deliver();

		assertThat(this.relay.deliver()).isZero();

		assertThat(available(Fixtures.A01)).isFalse();
	}

	@Test
	void a_refused_booking_records_nothing() {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");
		this.relay.deliver();

		try {
			this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "grace@example.com");
		}
		catch (RuntimeException expected) {
			// the desk is taken
		}

		assertThat(this.outbox.unpublished(10)).isEmpty();
	}

}
