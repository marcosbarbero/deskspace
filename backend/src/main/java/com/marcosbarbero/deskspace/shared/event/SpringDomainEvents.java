package com.marcosbarbero.deskspace.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The in-process adapter, used when there is no database.
 *
 * With one, OutboxDomainEvents takes over: publishing straight to subscribers would put
 * delivery outside the transaction that made the change durable.
 *
 * The original note still applies here. Publication is synchronous and inside the
 * caller's transaction, which is a deliberate choice recorded in
 * docs/adr/0009-events-between-slices.md: the read model is never stale in a way a user
 * could observe, and the day that stops being affordable, only this class changes.
 */
@Component
@Profile("!postgres")
public class SpringDomainEvents implements DomainEvents {

	private final ApplicationEventPublisher publisher;

	public SpringDomainEvents(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publish(DomainEvent event) {
		this.publisher.publishEvent(event);
	}

}
