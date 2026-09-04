package com.marcosbarbero.deskspace.shared.event.outbox;

import tools.jackson.databind.ObjectMapper;
import com.marcosbarbero.deskspace.shared.event.DomainEvent;
import com.marcosbarbero.deskspace.shared.event.DomainEvents;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * The publisher a use case sees when there is a database behind it.
 *
 * It does not publish. It records, in the transaction the caller is already in, so the
 * event becomes durable at exactly the moment the change that caused it does. Delivery is
 * somebody else's job and can be retried.
 *
 * The use case is unchanged and cannot tell. That is the whole reason
 * {@link DomainEvents} is a port.
 */
@Component
@Profile("postgres")
public class OutboxDomainEvents implements DomainEvents {

	private final Outbox outbox;

	private final ObjectMapper json;

	public OutboxDomainEvents(Outbox outbox, ObjectMapper json) {
		this.outbox = outbox;
		this.json = json;
	}

	@Override
	public void publish(DomainEvent event) {
		this.outbox.record(
				new StoredEvent(UUID.randomUUID(), event.getClass().getName(), serialise(event), event.occurredAt()));
	}

	// An event that cannot be written is a programming error. Letting it throw
	// rolls back the change that produced it, which is the correct outcome: a
	// change nobody can be told about should not be committed.
	private String serialise(DomainEvent event) {
		return this.json.writeValueAsString(event);
	}

}
