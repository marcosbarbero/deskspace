# 0007. Consumer-driven contract tests alongside the spec

- Status: Accepted
- Date: 2026-02-20

## Context

[0002](0002-the-spec-is-the-contract.md) makes `api/openapi.yaml` the source both
sides generate from, which removes a whole class of drift: neither side can name
a field the spec does not have.

It does not remove all of it. A spec describes what the API *may* do, and it is
deliberately permissive: `POST /api/bookings` documents `201`, `400` and `409`,
because all three are real. So a change from `409` to `400` on the
already-booked path produces a service that still matches the spec exactly, still
generates identical types, still passes every test on both sides, and breaks the
one screen that has to tell a member the desk went while they were reading.

That was verified rather than assumed. Making that change and running the
provider verification produces:

```
1.1) status: expected status of 409 but was 400
```

Nothing else in either build noticed.

## Options considered

### A. The spec plus consumer-driven contract tests (chosen)

The consumer publishes what it actually depends on. The provider verifies it.
The spec stays the description of the API; the pact becomes the description of
the coupling, which is a smaller and stricter thing.

### B. Tighten the spec until it is exact (rejected)

Remove `400` from the endpoint so `409` is the only failure. It works for this
one case and it degrades the spec: statuses that are genuinely possible get
deleted from the document to make it act as a test, so the document stops being
true. A spec that lies to serve as a gate is worse than a permissive spec plus a
real gate.

### C. End-to-end tests against a running pair (rejected as the answer)

They catch this and much more, and they are slow, flaky, and require both sides
running. Worth having for a handful of journeys, and not a replacement for a
check that runs in a second in each build independently.

### D. Trust integration in the deployed environment (rejected)

That is not a check, it is a place where the failure is discovered by a user.

## Decision

The front end publishes a pact to `pacts/`. `BookingContractVerificationTest`
verifies it against a real Spring context on a random port, with provider states
that set up the data each interaction assumes.

The clock in that test is fixed to the date the pact names. Otherwise the
contract rots on a calendar rather than on a change somebody made, which is the
worst kind of red build: nobody did anything and it broke.

## Consequences

The pact is deliberately loose about values and strict about shape and status.
Changing the wording of a problem title does not fail verification, and it should
not: `docs/ux/desk-board.md` says the screen renders whatever title the API
sends, so the wording is not part of the coupling. Renaming a field or changing a
status does fail, because those are.

There is no Pact broker. The pact file is committed, which works because both
sides live in one repository and are released together. A second consumer, or a
separate release cycle, is the point at which a broker stops being optional.

Provider states are code the production service does not have. `deleteAll()` on
the booking repository exists for them. That is a real cost of this decision and
it is small.
