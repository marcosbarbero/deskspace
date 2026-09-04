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

	private final OutboxPruner pruner;

	public ScheduledOutboxRelay(OutboxRelay relay, OutboxPruner pruner) {
		this.relay = relay;
		this.pruner = pruner;
	}

	@Scheduled(fixedDelayString = "${deskspace.outbox.interval:PT1S}")
	void deliver() {
		this.relay.deliver();
	}

	/**
	 * Far less often than delivery. Pruning is housekeeping and a delay in it costs disk;
	 * a delay in delivery costs correctness.
	 */
	@Scheduled(fixedDelayString = "${deskspace.outbox.prune-interval:PT1H}")
	void prune() {
		this.pruner.prune();
	}

}
