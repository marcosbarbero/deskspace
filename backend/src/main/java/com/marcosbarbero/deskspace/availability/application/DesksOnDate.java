package com.marcosbarbero.deskspace.availability.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.marcosbarbero.deskspace.availability.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.availability.application.port.out.OccupiedDesks;
import com.marcosbarbero.deskspace.availability.domain.DeskAvailability;

import org.springframework.stereotype.Service;

/**
 * The read side.
 *
 * It answers from its own projection and its own view of the room. It asks the booking
 * slice nothing, which is why the two could be deployed apart without this class
 * changing.
 */
@Service
public class DesksOnDate {

	private final DeskDirectory desks;

	private final OccupiedDesks occupied;

	public DesksOnDate(DeskDirectory desks, OccupiedDesks occupied) {
		this.desks = desks;
		this.occupied = occupied;
	}

	/**
	 * @param zone limit the answer to one zone, or null for every zone. Null rather than
	 * an empty string on purpose: "no filter" and "a filter matching nothing" are
	 * different answers, and a caller must be able to say which it meant.
	 */
	public List<DeskAvailability> on(LocalDate date, String zone) {
		Set<UUID> taken = this.occupied.on(date);
		return this.desks.all()
			.stream()
			.filter((desk) -> zone == null || desk.zone().equals(zone))
			.map((desk) -> DeskAvailability.of(desk, !taken.contains(desk.id())))
			.toList();
	}

}
