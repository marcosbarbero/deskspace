package com.marcosbarbero.deskspace.availability.domain;

import java.util.UUID;

/** One row of the answer to "what can I take on this date". */
public record DeskAvailability(UUID deskId, String label, String zone, boolean available) {

	public static DeskAvailability of(DeskSummary desk, boolean available) {
		return new DeskAvailability(desk.id(), desk.label(), desk.zone(), available);
	}
}
