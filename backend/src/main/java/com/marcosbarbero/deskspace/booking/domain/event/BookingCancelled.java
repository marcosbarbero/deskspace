package com.marcosbarbero.deskspace.booking.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.marcosbarbero.deskspace.shared.event.DomainEvent;

public record BookingCancelled(UUID bookingId, UUID deskId, LocalDate date, Instant occurredAt) implements DomainEvent {
}
