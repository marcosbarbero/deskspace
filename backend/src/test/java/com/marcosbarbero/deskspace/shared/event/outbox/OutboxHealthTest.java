package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A stalled relay produces no errors. Every request keeps returning 200 and the read
 * model falls further behind by the minute, so age is the only signal there is, and this
 * is where it is turned into something a monitor can see.
 */
class OutboxHealthTest {

	private static final Instant NOW = Instant.parse("2026-03-02T09:00:00Z");

	private static final Duration THRESHOLD = Duration.ofMinutes(1);

	private OutboxHealth health(Backlog backlog) {
		return new OutboxHealth(new FixedBacklog(backlog), Clock.fixed(NOW, ZoneOffset.UTC), THRESHOLD);
	}

	@Test
	void an_empty_outbox_is_up() {
		assertThat(health(Backlog.EMPTY).health().getStatus()).isEqualTo(Status.UP);
	}

	@Test
	void a_fresh_backlog_is_up_because_a_busy_moment_is_not_a_fault() {
		Backlog fresh = new Backlog(12, Optional.of(NOW.minusSeconds(5)));

		assertThat(health(fresh).health().getStatus()).isEqualTo(Status.UP);
	}

	@Test
	void an_old_backlog_is_down() {
		Backlog stalled = new Backlog(1, Optional.of(NOW.minusSeconds(120)));

		assertThat(health(stalled).health().getStatus()).isEqualTo(Status.DOWN);
	}

	@Test
	void the_report_names_the_backlog_and_the_threshold() {
		Backlog stalled = new Backlog(3, Optional.of(NOW.minusSeconds(120)));

		assertThat(health(stalled).health().getDetails()).containsEntry("waiting", 3)
			.containsEntry("oldestSeconds", 120L)
			.containsEntry("stalledAfterSeconds", 60L);
	}

	@Test
	void exactly_at_the_threshold_is_still_up() {
		Backlog borderline = new Backlog(1, Optional.of(NOW.minus(THRESHOLD)));

		assertThat(health(borderline).health().getStatus()).isEqualTo(Status.UP);
	}

	private record FixedBacklog(Backlog backlog) implements Outbox {

		@Override
		public void record(StoredEvent event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<StoredEvent> unpublished(int limit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void markPublished(UUID id, Instant at) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int prunePublishedBefore(Instant cutoff) {
			throw new UnsupportedOperationException();
		}

	}

}
