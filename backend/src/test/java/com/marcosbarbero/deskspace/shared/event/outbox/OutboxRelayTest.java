package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;
import com.marcosbarbero.deskspace.shared.event.DomainEvent;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Delivery, with a list standing in for the table.
 *
 * The relay has no schedule and no clock, so this needs no waiting: call deliver, assert
 * what came out. The scheduling lives in a separate class for exactly this reason.
 */
class OutboxRelayTest {

	private final ObjectMapper json = JsonMapper.builder().build();

	private final List<DomainEvent> published = new ArrayList<>();

	private InMemoryOutbox outbox;

	private OutboxRelay relay;

	@BeforeEach
	void setUp() {
		this.outbox = new InMemoryOutbox();
		ApplicationEventPublisher publisher = (event) -> this.published.add((DomainEvent) event);
		this.relay = new OutboxRelay(this.outbox, publisher, this.json);
	}

	private StoredEvent stored(Instant at) {
		DeskBooked event = new DeskBooked(UUID.randomUUID(), Fixtures.A01, Fixtures.TODAY, at);
		return new StoredEvent(UUID.randomUUID(), DeskBooked.class.getName(), this.json.writeValueAsString(event), at);
	}

	@Test
	void publishes_what_is_waiting() {
		this.outbox.record(stored(Instant.EPOCH));

		assertThat(this.relay.deliver()).isEqualTo(1);
		assertThat(this.published).singleElement().isInstanceOf(DeskBooked.class);
	}

	@Test
	void reads_the_event_back_with_its_fields_intact() {
		this.outbox.record(stored(Instant.EPOCH));

		this.relay.deliver();

		DeskBooked delivered = (DeskBooked) this.published.get(0);
		assertThat(delivered.deskId()).isEqualTo(Fixtures.A01);
		assertThat(delivered.date()).isEqualTo(Fixtures.TODAY);
		assertThat(delivered.occurredAt()).isEqualTo(Instant.EPOCH);
	}

	@Test
	void marks_what_it_published_so_a_second_run_does_nothing() {
		this.outbox.record(stored(Instant.EPOCH));
		this.relay.deliver();

		assertThat(this.relay.deliver()).isZero();
		assertThat(this.published).hasSize(1);
	}

	@Test
	void publishes_oldest_first() {
		StoredEvent later = stored(Instant.EPOCH.plusSeconds(60));
		StoredEvent earlier = stored(Instant.EPOCH);
		this.outbox.record(later);
		this.outbox.record(earlier);

		this.relay.deliver();

		assertThat(this.published).hasSize(2);
		assertThat(((DeskBooked) this.published.get(0)).occurredAt()).isEqualTo(Instant.EPOCH);
	}

	@Test
	void an_empty_outbox_delivers_nothing() {
		assertThat(this.relay.deliver()).isZero();
		assertThat(this.published).isEmpty();
	}

	/** A list with the three methods the port needs. */
	private static final class InMemoryOutbox implements Outbox {

		private final List<StoredEvent> rows = new ArrayList<>();

		private final List<UUID> published = new ArrayList<>();

		@Override
		public void record(StoredEvent event) {
			this.rows.add(event);
		}

		@Override
		public List<StoredEvent> unpublished(int limit) {
			return this.rows.stream()
				.filter((row) -> !this.published.contains(row.id()))
				.sorted((a, b) -> a.occurredAt().compareTo(b.occurredAt()))
				.limit(limit)
				.toList();
		}

		@Override
		public void markPublished(UUID id) {
			this.published.add(id);
		}

	}

}
