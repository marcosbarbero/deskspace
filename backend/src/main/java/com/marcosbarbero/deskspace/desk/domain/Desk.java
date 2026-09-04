package com.marcosbarbero.deskspace.desk.domain;

import java.util.UUID;

/**
 * A physical desk. Knows nothing about bookings: a desk exists whether or not anybody
 * took it, and availability is a different slice's question.
 */
public record Desk(UUID id, String label, Zone zone) {
}
