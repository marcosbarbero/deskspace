# 0009. Slices talk by events, and availability is a read model

- Status: Accepted
- Date: 2026-03-06

## Context

Availability needs to know which desks are taken. The obvious implementation is
the one that was here: ask the booking slice.

It works, and it welds the two together. Availability cannot be read while
booking is down, cannot be deployed separately, and cannot be scaled for a
read-heavy load independently of the write path, which is the load it actually
has. Every one of those is a decision nobody made.

## Options considered

### A. Booking publishes events; availability keeps its own read model (chosen)

`DeskBooked` and `BookingCancelled` are facts booking announces. Availability
subscribes and maintains a set of occupied desks per date. Neither slice calls
the other, in either direction.

### B. Availability queries the booking repository (rejected)

What was here. Simple, correct, and it makes the coupling a method signature
rather than a data shape. It also makes the architecture rule unstatable: you
cannot forbid availability from depending on booking when its whole
implementation is a call into booking.

### C. Booking maintains the availability view itself (rejected)

Removes a slice and gives the write side a second job. Booking would then know
what a read model is and would have to be changed whenever the read side wanted
a different shape, which is the coupling from B with the direction reversed and
no test able to see it.

### D. An outbox and a broker now (rejected, for now)

The right answer for two deployables. Today they are one, so a broker would add
a moving part, a delivery guarantee to reason about and a docker dependency in
the README, to solve a problem this repository does not yet have.

## Decision

Booking publishes; availability subscribes and projects. Events live in
`booking.domain.event`, which is the only package in booking that anything else
may import, and `ArchitectureRulesTest` says so.

Publication is in-process and synchronous, through a `DomainEvents` port with a
Spring adapter behind it. The read model is therefore never observably stale.

## Consequences

Events carry ids and dates, not the `Booking`. Passing the aggregate would make
every field of an internal type part of the contract between slices, and the
first rename would be a cross-slice breaking change.

The projection is idempotent, and tested for it, even though in process an event
arrives exactly once. That is deliberate: at-least-once delivery is what a broker
gives you, and the property should already be proven and tested before the day
that matters.

Two things now need a test that neither slice needs alone. `BookingReachesAvailabilityTest`
boots the container and books through the real use case, because "booking
publishes" and "the projection reacts" can both be green while nothing is
subscribed, and that failure looks like nothing at all until a screen shows a
desk that was taken an hour ago.

Moving to a broker changes `SpringDomainEvents` and `BookingEventListener` and
nothing else. That is the whole reason the port exists.
