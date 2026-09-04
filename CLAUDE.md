# Deskspace

Desk booking. A Spring Boot API and a React front end with **one contract
between them**, and gates that decide "done" without a human reading a diff.

This file is loaded on every turn, so it stays short. Anything that can be
looked up on demand lives in a registry, not here.

## The one rule that explains the others

**The contract is `api/openapi.yaml`.** Both sides are generated from it. You
never hand-write a wire type, and you never edit generated code. A change to the
API starts by editing the spec.

## Where things are

| | |
|---|---|
| `api/openapi.yaml` | the contract |
| `backend/src/main/java/.../{desk,booking,availability,shared}` | feature slices |
| `frontend/src/features/`, `frontend/src/api/` | feature slices, generated client |
| `pacts/` | what the front end depends on; the backend verifies it |
| `docs/` | PRD, journeys, UX, ADRs, lexicon. `docs/README.md` says which is which |

Three registries, three questions. Look things up rather than searching:

```bash
toolbox/tool_mapping.py list           # what tools exist
toolbox/arch_map.py get booking        # where the code lives, and what may depend on it
toolbox/lexicon.py search "checkstyle" # how we solved this before
```

## Definition of done

```bash
toolbox/verify              # format, lint, architecture, tests, coverage, contract, spec drift
toolbox/verify --mutation   # the above plus the mutation score
```

Green means done. There is no second opinion, and CI runs the same script.

## Hard rules

1. **Test first.** A `PreToolUse` hook rejects edits to `backend/src/main/java`
   or `frontend/src` when the branch has no test change. It is a gate, not a
   preference, and it is not negotiable by prompting.
2. **Never edit a gate to make it pass.** `.githooks/`, `.claude/hooks/`,
   `ArchitectureRulesTest`, the thresholds in `backend/pom.xml`, and `toolbox/verify`
   are denied. Moving the gate is not passing it.
3. **Never weaken a test or a threshold** to get green. If a threshold is wrong,
   say so and stop.
4. **Search the lexicon before deriving.** Add to it before you finish, whenever
   something surprised you.
5. **A change that touches the wire starts at the spec**, then regenerates both
   sides. `toolbox/verify` fails if the committed client is stale.
6. **Refuse a ticket rather than guessing.** Missing requirements are a defect in
   the ticket, not something to improvise around.

## Shape

Slices are features. Inside a slice, dependencies point inward:

```
<slice>/domain/          types and rules, depending on nothing of ours
        domain/event/    the published language other slices may read
        application/     use cases, and the ports they declare
          port/out/      interfaces named for what this slice needs
        adapter/in/      HTTP and events arrive here and stop here
        adapter/out/     storage, other slices, the publisher
```

**Slices talk by events, never by calls.** Booking publishes `DeskBooked` and
`BookingCancelled`; availability subscribes and keeps its own read model. The
only thing availability may import from booking is `booking.domain.event`, and
crossing a slice boundary at all belongs in exactly one adapter class.

## Conventions

- Constructor injection, no field injection. Records for domain types.
- An application layer never names an adapter and never imports another slice.
  If it needs something, it declares a port and an adapter implements it.
- Wire types carry the `Api` prefix and stop at `adapter/in/web`.
- Non-determinism is injected: `Clock` and the id supplier are beans. Nothing
  reads the wall clock, in production or in a test.
- Events are records in `domain/event`, carrying ids and dates rather than an
  aggregate. Handlers are idempotent.
- Every error response is an `ApiProblem`, and each slice translates its own
  failures. A shared advice class importing every slice creates a cycle.
- TypeScript: no floating promises, no `any`, generated types are never edited.
