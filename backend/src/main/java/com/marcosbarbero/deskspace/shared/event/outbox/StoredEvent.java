package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Instant;
import java.util.UUID;

/**
 * An event as it sits in the outbox: an id, the type needed to read it back, the payload,
 * and when it happened.
 */
public record StoredEvent(UUID id, String type, String payload, Instant occurredAt) {
}
