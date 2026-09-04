package com.marcosbarbero.deskspace.shared.event.outbox;

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

	void markPublished(UUID id);

}
