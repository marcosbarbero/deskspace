package com.marcosbarbero.deskspace.shared.event;

import java.time.Instant;

/**
 * Something that happened, stated in the past tense, owned by the slice that decided it.
 *
 * Events are the only thing one slice publishes to another. A slice never calls into
 * another slice's application layer, so the coupling is a data shape rather than a method
 * signature, and it is the shape that would survive being put on a broker. See
 * docs/adr/0009-events-between-slices.md.
 */
public interface DomainEvent {

	Instant occurredAt();

}
