# 0011. An outbox, so an event is as durable as the change that caused it

- Status: Accepted
- Date: 2026-03-18
- Refines: [0009](0009-events-between-slices.md), [0010](0010-postgres-behind-a-profile.md)

## Context

[0009](0009-events-between-slices.md) made availability a read model fed by
booking's events. [0010](0010-postgres-behind-a-profile.md) put bookings in a
database. Together they left a window: the booking commits, then the event is
published, and the two are not one act.

A crash in that window leaves a booking that exists and a read model that will
never hear about it. Nothing retries, nothing notices, and the desk is free on a
screen forever. The window is small and it opens on every deploy, every eviction
and every out-of-memory kill.

## Options considered

### A. An outbox table written in the same transaction (chosen)

Recording an event inserts a row alongside the booking. Either both commit or
neither does. A relay reads unpublished rows and delivers them, marking each one
as it goes.

### B. Publish after commit, with a retry (rejected)

`@TransactionalEventListener(AFTER_COMMIT)` and a retry around the publish. It
narrows the window and does not close it: the retry state lives in the process
that is about to die. Nothing survives a kill between commit and the first
attempt.

### C. Write to the broker inside the transaction (rejected)

A distributed transaction between a database and a broker, which is either
unavailable or expensive, and is the thing the outbox pattern exists to avoid.

### D. Rebuild the read model from the write model on a schedule (rejected)

A reconciliation job. It genuinely fixes drift, and it makes the read model
eventually correct on a period rather than promptly, and it grows a second code
path that computes availability. Worth having as a repair tool one day. Not a
substitute for delivery.

## Decision

`OutboxDomainEvents` implements the existing `DomainEvents` port by recording
rather than publishing, so no use case learns that an outbox exists. `OutboxRelay`
delivers and marks. `ScheduledOutboxRelay` is what runs it in a deployed service.

Delivery is **at least once**, and cannot be anything else: publishing and marking
cannot be atomic with a subscriber outside the transaction. Every subscriber is
therefore idempotent, which `AvailabilityProjection` already was, with a test
that now earns its place instead of being a precaution.

## Consequences

The read model is now eventually consistent rather than immediately consistent.
Under the default profile it stays immediate, because `SpringDomainEvents`
publishes in process. That means the two profiles differ in a way that matters,
and it is stated here rather than discovered.

The relay carries no schedule, which is why every delivery test calls a method
and asserts instead of sleeping. The schedule lives in a separate class.

Events are stored with their class name, so renaming an event class strands the
rows already written. That is a real constraint on refactoring and the reason
`booking.domain.event` is published language rather than ordinary code.

The outbox grows forever. Nothing prunes it yet, which is fine at this size and
is the next thing this table needs.
