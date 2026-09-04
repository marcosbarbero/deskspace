package com.marcosbarbero.deskspace.shared.event.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Where events wait between being recorded and being delivered.
 *
 * Recording happens inside the caller's transaction; delivery happens later and may
 * happen more than once. Those are the two properties everything else here depends on.
 */
public interface Outbox {

	void record(StoredEvent event);

	List<StoredEvent> unpublished(int limit);

	/**
	 * @param at when it was delivered, from the injected clock. Not the database's
	 * `now()`: that is a second clock nobody chose, it disagrees with the one every test
	 * pins, and retention is measured against it.
	 */
	void markPublished(UUID id, Instant at);

	/**
	 * What is waiting, and since when. One call rather than two, because a count and an
	 * age read a moment apart can disagree in a way that is confusing to read and
	 * impossible to reproduce.
	 */
	Backlog backlog();

	/**
	 * Remove delivered rows older than the cutoff.
	 * @return how many were removed
	 */
	int prunePublishedBefore(Instant cutoff);

}
