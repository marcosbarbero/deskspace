package com.marcosbarbero.deskspace.shared.event.outbox;

import java.util.List;

import tools.jackson.databind.ObjectMapper;
import com.marcosbarbero.deskspace.shared.event.DomainEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Reads the outbox and delivers what is in it, oldest first.
 *
 * Delivery is at least once, deliberately and unavoidably: the publish and the mark
 * cannot be one atomic act with a subscriber outside the transaction, so a crash between
 * them redelivers. Every subscriber must therefore be idempotent, which
 * {@code AvailabilityProjection} is and has a test for.
 *
 * Not annotated with a schedule. What triggers a run is a transport decision and belongs
 * to the adapter that owns it, which keeps this class callable directly from a test with
 * no waiting and no clock.
 */
@Component
@Profile("postgres")
public class OutboxRelay {

	private static final int BATCH = 100;

	private final Outbox outbox;

	private final ApplicationEventPublisher publisher;

	private final ObjectMapper json;

	public OutboxRelay(Outbox outbox, ApplicationEventPublisher publisher, ObjectMapper json) {
		this.outbox = outbox;
		this.publisher = publisher;
		this.json = json;
	}

	/**
	 * @return how many events were delivered, so a caller can drain the outbox without
	 * sleeping.
	 */
	public int deliver() {
		List<StoredEvent> waiting = this.outbox.unpublished(BATCH);
		for (StoredEvent stored : waiting) {
			this.publisher.publishEvent(read(stored));
			this.outbox.markPublished(stored.id());
		}
		return waiting.size();
	}

	private DomainEvent read(StoredEvent stored) {
		try {
			Class<?> type = Class.forName(stored.type());
			return (DomainEvent) this.json.readValue(stored.payload(), type);
		}
		catch (ReflectiveOperationException | RuntimeException ex) {
			throw new IllegalStateException("Cannot read outbox row " + stored.id() + " of type " + stored.type(), ex);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot read outbox row " + stored.id(), ex);
		}
	}

}
