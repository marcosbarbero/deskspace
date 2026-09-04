# 0005. In-memory persistence for the reference implementation

- Status: Accepted
- Date: 2026-02-08

## Context

This repository exists to demonstrate a delivery harness. Every minute a reader
spends getting it to run is a minute not spent on the thing it is demonstrating,
and a fresh clone that needs a database is a fresh clone that does not go green
on the first try.

Against that: a store that cannot express a constraint cannot demonstrate a
constraint, and "no two confirmed bookings for the same desk on the same date" is
exactly the kind of rule that is interesting precisely because a real database
enforces it differently from application code.

## Options considered

### A. In-memory repositories (chosen)

`./mvnw test` works on any machine with a JDK, with no Docker and no setup. The
booking rules live in the service and are tested there.

### B. Postgres with Testcontainers by default (rejected)

The honest production shape, and it makes the first command in the README fail
for anyone without a running Docker daemon. The harness is what is being taught;
losing readers at step one to teach it is a bad trade.

### C. H2 in test, Postgres in production (rejected)

The worst of both. Tests pass against a database that behaves differently from
the one that runs in production, which is how a uniqueness constraint gets tested
in a dialect that is not the one enforcing it.

## Decision

Repositories are in memory. The uniqueness rule is enforced in `BookingService`
and tested there.

## Consequences

The service does not demonstrate transactional behaviour or constraint
violations arriving from the database, which is a real gap and is stated here
rather than glossed over.

If this repository ever grows a database, it will be Postgres with Testcontainers
behind an opt-in profile, so that a default clone stays green. That is a
consequence of this decision, not a new one.
