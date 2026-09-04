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
| both sides generated | Java interfaces and `Api*` types; TypeScript client types, from the same file |
| `pacts/` | what the front end actually depends on, verified against a real Spring context |
| `ArchitectureRulesTest` | 5 ArchUnit rules: slice isolation, no cycles, no field injection, domain free of wire types |
| format and lint | `spring-javaformat` validate, Checkstyle with an explicit ruleset, typed ESLint |
| coverage | JaCoCo, 85% line and 75% branch, generated code excluded |
| mutation | PIT, threshold 80, currently 100% of 32 mutants killed |
| `./verify` | one script that decides whether this repository is green |
| `.githooks/pre-push` | runs `./verify` before anything leaves the machine |
| documentation | PRD, journey, feature UX, 7 ADRs including one rejected and one superseded |

Still to come: `CLAUDE.md` and the `.claude/` harness, GitHub issue forms, the
workflow command, CI, and evals.

## One definition of green

```bash
./verify              # format, lint, architecture, tests, coverage, contract, types
./verify --mutation   # the above, plus the mutation score
git config core.hooksPath .githooks    # once per clone; git cannot do it for you
```

`./verify` is the only answer to "is it green". The hook runs it, CI runs it, you
run it. Two definitions of done drift the moment a gate is added to one of them.

The generated TypeScript client is committed **and** checked: `./verify`
regenerates it and fails if that produces a diff, because a spec change nobody
regenerated is a spec change the front end has not seen.

Renaming one field in `api/openapi.yaml` and running `./verify` fails three
gates at once: the backend no longer implements its generated interface, the
committed client types are stale, and the front end no longer typechecks. That
is the seam working.

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
