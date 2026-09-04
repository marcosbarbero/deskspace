package com.marcosbarbero.deskspace.availability.adapter.out.persistence;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.marcosbarbero.deskspace.availability.application.port.out.OccupiedDesks;

import org.springframework.stereotype.Component;

/**
 * The projection store. A set of desk ids per date, and nothing else: a read model holds
 * the answer, not a second copy of the write model.
 */
@Component
public class InMemoryOccupiedDesks implements OccupiedDesks {

	private final Map<LocalDate, Set<UUID>> byDate = new ConcurrentHashMap<>();

	@Override
	public Set<UUID> on(LocalDate date) {
		return Set.copyOf(this.byDate.getOrDefault(date, Set.of()));
	}

	@Override
	public void occupy(UUID deskId, LocalDate date) {
		this.byDate.computeIfAbsent(date, (key) -> ConcurrentHashMap.newKeySet()).add(deskId);
	}

	@Override
	public void release(UUID deskId, LocalDate date) {
		this.byDate.computeIfPresent(date, (key, desks) -> {
			desks.remove(deskId);
			return desks;
		});
	}

	@Override
	public void clear() {
		this.byDate.clear();
	}

}
