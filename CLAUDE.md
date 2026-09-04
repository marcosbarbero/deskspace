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
.claude/tools/tool_mapping.py list           # what tools exist
.claude/tools/arch_map.py get booking        # where the code lives, and what may depend on it
.claude/tools/lexicon.py search "checkstyle" # how we solved this before
```

## Definition of done

```bash
./verify              # format, lint, architecture, tests, coverage, contract, spec drift
./verify --mutation   # the above plus the mutation score
```

Green means done. There is no second opinion, and CI runs the same script.

## Hard rules

1. **Test first.** A `PreToolUse` hook rejects edits to `backend/src/main/java`
   or `frontend/src` when the branch has no test change. It is a gate, not a
   preference, and it is not negotiable by prompting.
2. **Never edit a gate to make it pass.** `.githooks/`, `.claude/hooks/`,
   `ArchitectureRulesTest`, the thresholds in `backend/pom.xml`, and `verify`
   are denied. Moving the gate is not passing it.
3. **Never weaken a test or a threshold** to get green. If a threshold is wrong,
   say so and stop.
4. **Search the lexicon before deriving.** Add to it before you finish, whenever
   something surprised you.
5. **A change that touches the wire starts at the spec**, then regenerates both
   sides. `./verify` fails if the committed client is stale.
6. **Refuse a ticket rather than guessing.** Missing requirements are a defect in
   the ticket, not something to improvise around.

## Conventions

- Java: constructor injection, no field injection. Records for domain types.
  Wire types carry the `Api` prefix and never leave a controller.
- Non-determinism is injected: `Clock` and the id supplier are beans. No test
  reads the wall clock.
- Every error response is an `ApiProblem`, because the spec promises it. The
  front end renders the problem title and never invents wording.
- TypeScript: no floating promises, no `any`, generated types are never edited.
