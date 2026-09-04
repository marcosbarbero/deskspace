# Deskspace — a harnessed full-stack service

A desk booking service for a shared workspace: a Spring Boot API and a React
front end, with **one contract between them** and a delivery harness around both.

The point of this repo is the harness and the seam, not the application.
[spring-petclinic-ai-ready](https://github.com/marcosbarbero/spring-petclinic-ai-ready)
demonstrates the same ideas on a single deployable. This one exists for what a
single deployable cannot show: two codebases that have to agree, and what it
takes to make an agent safe to point at both.

## The contract is a file

`api/openapi.yaml` is the source of truth. Nothing on either side of the wire is
written by hand:

- the backend generates its controller interfaces and wire types from it
- the front end generates its client and its types from it
- neither can drift from it without the build noticing

Wire types carry an `Api` prefix so an import tells you which side of the seam a
type belongs to. The domain does not depend on them, and an architecture test
fails the build if it starts to.

## Status

Under construction, in the open. What works today:

| | |
|---|---|
| `api/openapi.yaml` | the contract: desks, bookings, RFC 9457 problems |
| backend codegen | `openapi-generator` produces controller interfaces and `Api*` types into `target/` |
| backend slices | `desk`, `booking`, `availability`, `shared` |
| `ArchitectureRulesTest` | 5 ArchUnit rules: slice isolation, no cycles, no field injection, domain free of wire types |
| deterministic tests | injected `Clock` and id supplier, so no test depends on the wall clock |
| frontend codegen | `openapi-typescript` produces `src/api/schema.d.ts` from the same file |
| frontend feature | the desk board, with every state from `docs/ux/desk-board.md` |
| documentation | PRD, journey, feature UX, and six ADRs including one rejected and one superseded |

Still to come: consumer-driven contract tests, coverage and mutation thresholds,
formatting and lint gates, the pre-push hook, issue forms, the workflow commands,
CI, and evals.

## Documentation

`docs/README.md` is the map: which document answers which question, and which
three things deliberately have no document because they have an executable
equivalent.

The rule worth stealing is in `docs/adr/README.md`. **An ADR must record what was
rejected and why.** A set of ADRs where every status is `Accepted` is not
documentation, it is a journal: it tells you what happened and not what was
decided, so the option somebody rejected for a good reason gets proposed again
next quarter by somebody who had no way to know.

ADRs here are immutable. [0003](docs/adr/0003-wire-types-share-domain-names.md)
was wrong and is still there, marked superseded by
[0006](docs/adr/0006-prefix-generated-wire-types.md), because the record of having
believed it is part of the record.
[0004](docs/adr/0004-a-shared-types-package.md) is an entire ADR whose status is
`Rejected`, kept because "why don't we just publish a shared types package" is
asked about twice a year.

## Running it

```bash
cd backend && ./mvnw test    # unit tests plus the architecture rules
cd frontend && npm run gen   # regenerate the client types from the spec
npm test                     # component tests
npm run typecheck            # the spec's shape, checked against the code using it
```
