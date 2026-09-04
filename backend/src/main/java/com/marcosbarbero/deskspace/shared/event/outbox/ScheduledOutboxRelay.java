package com.marcosbarbero.deskspace.shared.event.outbox;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * What makes the relay run in a deployed service.
 *
 * Separate from {@link OutboxRelay} on purpose. The schedule is a transport decision, and
 * keeping it here means every test of delivery calls a method and asserts, instead of
 * sleeping and hoping.
 */
@Component
@Profile("postgres")
public class ScheduledOutboxRelay {

	private final OutboxRelay relay;

	public ScheduledOutboxRelay(OutboxRelay relay) {
		this.relay = relay;
	}

	@Scheduled(fixedDelayString = "${deskspace.outbox.interval:PT1S}")
	void run() {
		this.relay.deliver();
	}

}
