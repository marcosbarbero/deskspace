package com.marcosbarbero.deskspace.desk;

import java.util.UUID;

/**
 * A physical desk. Knows nothing about bookings: see the slice rules in
 * ArchitectureRulesTest and docs/adr/0001-package-by-feature.md.
 */
public record Desk(UUID id, String label, Zone zone) {
}
