package com.marcosbarbero.deskspace.availability.application.port.out;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Outbound port for the projection: which desks are taken on a date.
 *
 * This is a read model, not a copy of the bookings. It holds the smallest fact the query
 * needs, which is why {@code occupy} and {@code release} take ids and dates rather than
 * bookings.
 */
public interface OccupiedDesks {

	Set<UUID> on(LocalDate date);

	void occupy(UUID deskId, LocalDate date);

	void release(UUID deskId, LocalDate date);

	void clear();

}
