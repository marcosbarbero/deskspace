package com.marcosbarbero.deskspace.support;

import java.util.ArrayList;
import java.util.List;

import com.marcosbarbero.deskspace.shared.event.DomainEvent;
import com.marcosbarbero.deskspace.shared.event.DomainEvents;

/**
 * A list with a publish method.
 *
 * This is what the outbound port buys: asserting that a use case announced something
 * needs no container, no listener and no waiting, because the port is an interface the
 * use case declared rather than a framework class it called.
 */
public class RecordingDomainEvents implements DomainEvents {

	private final List<DomainEvent> published = new ArrayList<>();

	@Override
	public void publish(DomainEvent event) {
		this.published.add(event);
	}

	public List<DomainEvent> published() {
		return List.copyOf(this.published);
	}

	@SuppressWarnings("unchecked")
	public <T extends DomainEvent> List<T> of(Class<T> type) {
		return this.published.stream().filter(type::isInstance).map((event) -> (T) event).toList();
	}

}
