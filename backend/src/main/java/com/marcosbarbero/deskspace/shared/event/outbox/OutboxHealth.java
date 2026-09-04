package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Reports the service unhealthy when the outbox has stopped draining.
 *
 * A stalled relay produces no errors. Every request still returns 200, the write side is
 * fine, and the read model falls further behind by the minute. The only evidence is that
 * something has been waiting too long, so that is what is measured.
 *
 * A backlog on its own is not a problem: a busy moment produces one and the next run
 * clears it. Age is the signal.
 */
@Component("outbox")
@Profile("postgres")
public class OutboxHealth implements HealthIndicator {

	private final Outbox outbox;

	private final Clock clock;

	private final Duration threshold;

	public OutboxHealth(Outbox outbox, Clock clock,
			@Value("${deskspace.outbox.stalled-after:PT1M}") Duration threshold) {
		this.outbox = outbox;
		this.clock = clock;
		this.threshold = threshold;
	}

	@Override
	public Health health() {
		Backlog backlog = this.outbox.backlog();
		Health.Builder builder = backlog.isStalled(this.clock.instant(), this.threshold) ? Health.down() : Health.up();
		return builder.withDetail("waiting", backlog.waiting())
			.withDetail("oldestSeconds", backlog.ageAt(this.clock.instant()).toSeconds())
			.withDetail("stalledAfterSeconds", this.threshold.toSeconds())
			.build();
	}

}
