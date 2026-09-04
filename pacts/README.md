# pacts

What the front end actually depends on, as opposed to what the API is allowed to do.

The file here is produced by `frontend/src/api/contract.pact.test.ts` and verified
against a real Spring context by `BookingContractVerificationTest`. It is
committed because both sides live in this repository and are released together. A
second consumer, or separate release cycles, is the point at which a broker stops
being optional.

## Why this exists when there is already a spec

`api/openapi.yaml` documents both `400` and `409` on the booking endpoint, because
both are genuinely possible. Moving the already-booked path from `409` to `400`
leaves the service matching the spec, leaves both sides generating identical
types, passes every other test, and breaks the one screen that has to tell a
member the desk went while they were reading it.

One thing notices:

```
1.1) status: expected status of 409 but was 400
```

The pact is loose about values and strict about shape and status. Changing a
problem *title* does not fail verification and should not: the screen renders
whatever title it is sent, so wording is not part of the coupling.

See [ADR 0007](../docs/adr/0007-contract-tests-alongside-the-spec.md).
