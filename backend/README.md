# backend

Spring Boot 4.1, Java 21. The API described by [`api/openapi.yaml`](../api/openapi.yaml).

```bash
./mvnw test                  # unit tests, architecture rules, the seam, contract verification
./mvnw verify                # the above plus format, checkstyle, coverage
./mvnw -Pmutation verify     # plus the mutation score
./mvnw -Pdatabase test       # plus the tests against a real Postgres (needs Docker)
./mvnw spring-boot:run       # http://localhost:8080, no database
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Usually you want `toolbox/verify` from the repository root instead.

## Shape

**Slices, and inside each slice a hexagon.** Packages are features
([ADR 0001](../docs/adr/0001-package-by-feature.md)); inside a feature,
dependencies point inward
([ADR 0008](../docs/adr/0008-ports-and-adapters-within-each-slice.md)).

```
booking/
  domain/            types and rules. Depends on nothing of ours
    event/           the published language: what other slices may read
  application/       use cases, and the ports they declare
    port/out/        interfaces this slice needs, named for what it needs
  adapter/in/web/    HTTP arrives here and stops here
  adapter/out/       storage, and the one class that reaches another slice
```

`toolbox/arch_map.py get booking` prints the same thing without opening a file.

## Slices, and how they talk

| slice | responsibility |
|---|---|
| `desk` | the desks that exist. Knows nothing about anybody |
| `booking` | who may take which desk when. The write side |
| `availability` | what is free on a date. A read model |
| `shared` | the event contract, the two sources of non-determinism, the error shape |

**Availability never asks booking anything.** Booking publishes `DeskBooked` and
`BookingCancelled`; availability subscribes and keeps its own projection
([ADR 0009](../docs/adr/0009-events-between-slices.md)). The only thing
availability may import from booking is `booking.domain.event`, and an
architecture rule fails the build otherwise.

Publication is in-process and synchronous today, behind a `DomainEvents` port.
Moving to a broker changes two adapter classes and nothing above them.

## Three things that are not obvious

**Generated code lives in `target/`.** Controller interfaces and `Api*` wire
types come from the spec on every build, are never committed, and stop at
`adapter/in/web`.

**Nothing reads the wall clock.** `Clock` and the id supplier are beans, so every
test pins them and contract verification pins a date rather than watching it
drift into the past.

**The projection is idempotent and tested for it**, even though in process an
event arrives once. At-least-once delivery is what a broker gives you, and that
property should be proven before the day it matters.

## Persistence

Two adapters behind one port, chosen by profile
([ADR 0010](../docs/adr/0010-postgres-behind-a-profile.md)). No profile means
`InMemoryBookings` and no Docker; the `postgres` profile means `JdbcBookings`
and Flyway migrations in `src/main/resources/db/migration`.

The application layer cannot tell which is behind it, and that is the claim the
whole arrangement makes. `JdbcBookingsTest` is where it is either true or not.

**The uniqueness rule is stated twice on purpose.** `BookDesk` checks before
writing, which is a check-then-act and therefore a race; a partial unique index
is what holds when two requests arrive together. The adapter translates the
duplicate-key violation into the same `DeskAlreadyBookedException` the service
raises, so the API answers 409 either way rather than 500.

The index is partial, `where status = 'CONFIRMED'`, because a plain constraint on
desk and day would make cancelling and rebooking the same desk on the same day
impossible.

## Where a test belongs

| the behaviour | the test |
|---|---|
| a booking rule | `BookDeskTest` — no Spring, four lines of setup |
| the read model reacting | `AvailabilityProjectionTest` — plain objects |
| a status code or problem body | `*ApiTest` — MockMvc, standalone |
| booking actually reaching availability | `BookingReachesAvailabilityTest` — the only test that boots the container for this |
| what the browser depends on | `BookingContractVerificationTest` — against `../pacts` |
