package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * What is waiting in the outbox, as one reading.
 *
 * @param waiting how many events have not been delivered
 * @param oldest when the oldest waiting event happened, or empty if none is
 */
public record Backlog(int waiting, Optional<Instant> oldest) {

	public static final Backlog EMPTY = new Backlog(0, Optional.empty());

	public Duration ageAt(Instant now) {
		return this.oldest.map((at) -> Duration.between(at, now)).orElse(Duration.ZERO);
	}

	public boolean isStalled(Instant now, Duration threshold) {
		return this.waiting > 0 && ageAt(now).compareTo(threshold) > 0;
	}
}
