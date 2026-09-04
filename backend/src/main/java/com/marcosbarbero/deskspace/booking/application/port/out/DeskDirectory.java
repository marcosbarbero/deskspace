package com.marcosbarbero.deskspace.booking.application.port.out;

import java.util.UUID;

/**
 * Outbound port: the one question booking asks about desks.
 *
 * Note how small it is. Booking does not need a desk, a label or a zone; it needs to know
 * whether an id names something real. Declaring the port from the caller's side is what
 * keeps that true, and it is why nothing in booking.application imports the desk slice.
 */
public interface DeskDirectory {

	boolean exists(UUID deskId);

}
