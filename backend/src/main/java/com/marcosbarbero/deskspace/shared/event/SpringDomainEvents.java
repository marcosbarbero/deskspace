package com.marcosbarbero.deskspace.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * The in-process adapter. Publication is synchronous and inside the caller's transaction,
 * which is a deliberate choice recorded in docs/adr/0009-events-between-slices.md: the
 * read model is never stale in a way a user could observe, and the day that stops being
 * affordable, only this class changes.
 */
@Component
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
