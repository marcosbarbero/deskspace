package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Removes delivered rows once they are older than the retention window.
 *
 * It can only ever remove rows that were published: the port has no way to express
 * anything else, which is deliberate. A retention window set absurdly short is then a
 * decision about history, never about delivery.
 *
 * Like the relay, this carries no schedule, so its test calls a method.
 */
@Component
@Profile("postgres")
public class OutboxPruner {

	private final Outbox outbox;

	private final Clock clock;

	private final Duration retention;

	public OutboxPruner(Outbox outbox, Clock clock, @Value("${deskspace.outbox.retention:P7D}") Duration retention) {
		this.outbox = outbox;
		this.clock = clock;
		this.retention = retention;
	}

	public int prune() {
		return this.outbox.prunePublishedBefore(this.clock.instant().minus(this.retention));
	}

}
