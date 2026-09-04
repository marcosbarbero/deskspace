# 0010. Postgres behind a profile, and the rule enforced twice

- Status: Accepted
- Date: 2026-03-12
- Refines: [0005](0005-in-memory-persistence.md)

## Context

[0005](0005-in-memory-persistence.md) chose in-memory repositories so a fresh
clone goes green without Docker, and stated the gap it was accepting: the service
could not demonstrate a constraint arriving from a database, and "no two
confirmed bookings for the same desk on the same day" is exactly that kind of
rule.

`BookDesk` checks it before writing. That is a check-then-act: two requests
arriving together both read "free" and both write. In memory it is invisible. In
production it is a double booking, and the member who loses finds out by
travelling in.

## Options considered

### A. Postgres behind a profile, with the rule in both places (chosen)

The default build has no database and needs no Docker. `-Pdatabase` and the
`postgres` profile swap in a JDBC adapter and run its tests against a real
Postgres through Testcontainers.

The service keeps its check, and a partial unique index enforces the same rule.
Two places, deliberately: the check gives a fast, specific failure on the common
path, the index is the one that holds under a race.

### B. Move the check into the database only (rejected)

Fewer places for the rule to be stated, and every rejection becomes an exception
from a driver that some adapter has to translate anyway. It also makes the rule
invisible in the use case, where somebody reading the booking logic should be
able to see it.

### C. Keep the service check only, and add a lock (rejected)

An application lock across two requests is a distributed lock with a lifetime,
and getting that wrong fails in a way that is much harder to see than a rejected
insert.

### D. Postgres by default, no in-memory adapter (rejected)

Honest about production and it costs the property 0005 bought: a clone that goes
green on any machine with a JDK. The cost lands on every reader, forever, to
serve a case a profile already covers.

## Decision

Two adapters behind one port, chosen by profile. The rule is stated twice, and
the JDBC adapter translates a duplicate-key violation into the same
`DeskAlreadyBookedException` the service raises, so the API answers 409 either
way rather than 500 under a race.

The index is partial, `where status = 'CONFIRMED'`. A plain unique constraint on
desk and day would make cancelling and rebooking the same desk on the same day
impossible, which is a thing members do.

## Consequences

The rule is in two places and can disagree. The mitigation is that the adapter
test asserts the database's answer is the same exception, so a divergence fails
the build rather than being discovered by a user.

Migrations are versioned and validated on start, so an edited migration that has
already been applied stops the application rather than leaving a schema nobody
can reproduce.

The default build stays Docker-free. CI runs `toolbox/verify --all`, which
includes the database tests, so nothing reaches main having only been checked
against a map.

Two things this still does not demonstrate: a transaction spanning more than one
write, and events surviving a crash between the write and the publish. The second
is what an outbox is for, and it is the next thing worth building here.
