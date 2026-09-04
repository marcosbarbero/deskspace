package com.marcosbarbero.deskspace.booking.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.marcosbarbero.deskspace.shared.event.DomainEvent;

/**
 * Published language. Other slices may depend on this package and on nothing else in
 * booking, which is asserted by ArchitectureRulesTest.
 *
 * It carries the facts a subscriber needs and no more. Passing the whole {@code Booking}
 * would make every field of an internal type part of the contract between slices.
 */
public record DeskBooked(UUID bookingId, UUID deskId, LocalDate date, Instant occurredAt) implements DomainEvent {
}
