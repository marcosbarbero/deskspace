# 0012. Operating the outbox: retention from delivery, and age as the alarm

- Status: Accepted
- Date: 2026-03-24
- Refines: [0011](0011-an-outbox-for-events.md)

## Context

[0011](0011-an-outbox-for-events.md) made events durable and left two things
undone, which are really one thing: the outbox is a queue, and nobody was
watching it or emptying it.

A queue that stops draining raises no error. Every request keeps returning 200,
the write side is fine, and the read model falls behind by the minute. The relay
could have been dead since Tuesday and the only symptom is a desk that looks
free. Meanwhile every row ever delivered is still there.

## Options considered

### A. Delete on delivery (rejected)

The simplest possible retention: mark and delete in one act. It throws away the
only record of what was sent, which is the first thing anybody asks for when a
subscriber disagrees with the write side.

### B. Retention measured from when the event happened (rejected)

The obvious reading of "keep events for seven days", and it punishes exactly the
case retention should protect. A consumer that was down for a week comes back to
find its backlog has been deleted for being old. Delivery is the thing that
finished; age of the event is not.

### C. Retention measured from delivery (chosen)

`published_at < now - retention`. A row is kept for a fixed period *after* it was
delivered, so nothing undelivered can ever be removed. That is a property of the
query, not of the configuration: there is no retention value that could delete a
waiting event.

### D. Alert on backlog size (rejected)

The metric people reach for, and it is noisy in both directions. A busy minute
produces a backlog that clears itself; a relay that died holding one event
produces a backlog of one. Size does not distinguish them.

### E. Alert on the age of the oldest waiting event (chosen)

A stalled relay is exactly "something has been waiting too long". It is quiet
during a burst and loud when delivery stops, which is the shape an alarm should
have.

## Decision

Retention is measured from delivery, pruning runs on a much longer schedule than
delivery, and health is down when the oldest waiting event is older than a
threshold. Both windows are configuration; neither can affect correctness.

The health detail reports what is waiting, how old it is, and what the threshold
is, so somebody reading it at three in the morning does not have to look up what
the number means.

## Consequences

Two clocks would have made all of this untestable, and there nearly were.
`markPublished` originally set `published_at = now()` in SQL, which no test can
pin, so retention was measured against a time the service did not choose. The
port now takes the instant, so one clock decides. Recorded as
`now-in-sql-is-a-clock-you-did-not-inject`.

Health being down for a backlog is a real operational decision: a service whose
read model is falling behind will be taken out of a load balancer by this. That
is intended. The alternative, a service that reports healthy while quietly
serving stale availability, is worse and much harder to notice.

The pruner deletes in one statement, which is fine at this size and will not be
at a size where the delete needs batching. Named here so the next person knows
it was a choice.
