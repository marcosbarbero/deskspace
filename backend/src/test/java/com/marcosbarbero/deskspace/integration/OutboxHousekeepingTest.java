package com.marcosbarbero.deskspace.integration;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.marcosbarbero.deskspace.shared.event.outbox.Backlog;
import com.marcosbarbero.deskspace.shared.event.outbox.Outbox;
import com.marcosbarbero.deskspace.shared.event.outbox.OutboxRelay;
import com.marcosbarbero.deskspace.shared.event.outbox.StoredEvent;
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
 * Pruning and the backlog reading, against a real Postgres.
 *
 * The rule that matters is the third test. A retention window is a decision about how
 * much history to keep, and it must never become a decision about delivery however short
 * somebody sets it.
 */
@Tag("database")
@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres")
@Import(OutboxHousekeepingTest.FixedClock.class)
class OutboxHousekeepingTest {

	private static final Instant NOW = Instant.parse("2026-03-02T09:00:00Z");

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
			return Clock.fixed(NOW, java.time.ZoneOffset.UTC);
		}

	}

	@MockitoBean
	private com.marcosbarbero.deskspace.shared.event.outbox.ScheduledOutboxRelay scheduled;

	@Autowired
	private Outbox outbox;

	@Autowired
	private OutboxRelay relay;

	@Autowired
	private com.marcosbarbero.deskspace.shared.event.outbox.OutboxPruner pruner;

	@BeforeEach
	void clean() {
		while (this.relay.deliver() > 0) {
			// drain
		}
		this.outbox.prunePublishedBefore(NOW.plus(365, ChronoUnit.DAYS));
	}

	private StoredEvent event(Instant at) {
		return new StoredEvent(UUID.randomUUID(), "com.marcosbarbero.deskspace.booking.domain.event.DeskBooked", """
				{"bookingId":"%s","deskId":"%s","date":"%s","occurredAt":"%s"}""".formatted(UUID.randomUUID(),
				Fixtures.A01, Fixtures.TODAY, at), at);
	}

	/**
	 * Retention is measured from delivery, not from when the event happened. A row
	 * delivered a minute ago is kept however old the event is, which is what protects a
	 * consumer that has been slow rather than punishing it.
	 */
	@Test
	void a_row_delivered_outside_the_window_is_removed() {
		StoredEvent old = event(NOW.minus(30, ChronoUnit.DAYS));
		this.outbox.record(old);
		this.outbox.markPublished(old.id(), NOW.minus(30, ChronoUnit.DAYS));

		assertThat(this.pruner.prune()).isEqualTo(1);
		assertThat(this.outbox.backlog()).isEqualTo(Backlog.EMPTY);
	}

	@Test
	void a_row_delivered_inside_the_window_is_kept() {
		StoredEvent recent = event(NOW.minus(30, ChronoUnit.DAYS));
		this.outbox.record(recent);
		this.outbox.markPublished(recent.id(), NOW.minus(1, ChronoUnit.DAYS));

		assertThat(this.pruner.prune()).isZero();
	}

	@Test
	void an_event_delivered_just_now_is_kept_however_old_the_event_is() {
		StoredEvent ancient = event(NOW.minus(3650, ChronoUnit.DAYS));
		this.outbox.record(ancient);
		this.relay.deliver();

		assertThat(this.pruner.prune()).isZero();
	}

	@Test
	void a_waiting_event_is_never_pruned_however_old() {
		this.outbox.record(event(NOW.minus(3650, ChronoUnit.DAYS)));

		assertThat(this.outbox.prunePublishedBefore(NOW.plus(3650, ChronoUnit.DAYS))).isZero();
		assertThat(this.outbox.backlog().waiting()).isEqualTo(1);
	}

	@Test
	void a_drained_outbox_reports_no_backlog() {
		this.outbox.record(event(NOW.minusSeconds(10)));
		this.relay.deliver();

		assertThat(this.outbox.backlog()).isEqualTo(Backlog.EMPTY);
	}

	@Test
	void the_backlog_reports_the_oldest_waiting_event() {
		this.outbox.record(event(NOW.minusSeconds(30)));
		this.outbox.record(event(NOW.minusSeconds(90)));

		Backlog backlog = this.outbox.backlog();

		assertThat(backlog.waiting()).isEqualTo(2);
		assertThat(backlog.ageAt(NOW).toSeconds()).isEqualTo(90);
	}

}
