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

What works today:

| | |
|---|---|
| `api/openapi.yaml` | the contract: desks, bookings, RFC 9457 problems |
| both sides generated | Java interfaces and `Api*` types; TypeScript client types, from the same file |
| `pacts/` | what the front end depends on, verified against a real Spring context |
| `ArchitectureRulesTest` | 5 ArchUnit rules: slice isolation, no cycles, no field injection, domain free of wire types |
| format and lint | `spring-javaformat`, Checkstyle, typed ESLint |
| coverage / mutation | JaCoCo 85% line, PIT threshold 80 and currently 100% of 32 mutants |
| `./verify` | the one script that decides whether this repository is green |
| `.githooks/pre-push`, CI | both run `./verify`, so there is no second opinion |
| `CLAUDE.md` | 67 lines, loaded every turn, pointing at registries rather than listing things |
| `.claude/hooks/` | test-first as `exit 2`, per side of the wire; formatting after every edit |
| `.claude/tools/` | four registries and reports, discovered by key |
| `.claude/commands/work.md` | `/work gh#42`: issue in, pull request out |
| `.claude/agents/` | `tech-lead` implements, `reviewer` checks requirements against asserting tests |
| `.github/ISSUE_TEMPLATE/` | an issue form whose fields a script validates before work starts |
| `docs/` | PRD, journey, feature UX, 7 ADRs including one rejected and one superseded, 7 lexicon entries |

Still to come: evals for the agents, and a second consumer to make the case for a
Pact broker.

## One definition of green

```bash
git config core.hooksPath .githooks    # once per clone; git cannot do it for you
./verify              # registries, format, lint, architecture, tests, coverage, contract, spec drift
./verify --mutation   # the above plus the mutation score
```

`./verify` is the only answer to "is it green". The hook runs it, CI runs it, you
run it. Two definitions of done drift the moment a gate is added to one of them.

## What the seam actually buys

The generated TypeScript client is committed **and** checked: `./verify`
regenerates it and fails on a diff, because a spec change nobody regenerated is a
spec change the front end has not seen.

Rename one field in `api/openapi.yaml` and run `./verify`: three gates fail at
once. The backend no longer implements its generated interface, the committed
client types are stale, and the front end stops typechecking.

The spec cannot catch everything, which is why the pacts exist. `POST
/api/bookings` documents `400` and `409`, both genuinely possible. Moving the
already-booked path from `409` to `400` leaves the service matching the spec,
leaves both sides generating identical types, and breaks the one screen that has
to tell a member the desk went while they were reading it. One thing notices:

```
1.1) status: expected status of 409 but was 400
```

## The harness

Three ideas, in order of how much they change:

**Gates decide done, not people.** Formatting, boundaries, coverage and mutation
are settled before a human looks at anything, so review is about whether the
change does what the ticket asked. `.claude/agents/reviewer.md` is explicitly
told not to re-review any of it.

**Test-first is `exit 2`, not a request.** A `PreToolUse` hook rejects edits to
production source on a branch with no test change **on that side of the wire**.
A React test does not unlock Java. Most single-service harnesses get this wrong
the moment a second deployable appears.

**Three registries, three questions.** What tools exist, where the code lives,
how we solved this before. Each has a lookup tool and a `check` that fails the
build on drift, because an unregistered tool is an undiscoverable tool and a map
that disagrees with the tree is worse than no map.

```bash
.claude/tools/tool_mapping.py list
.claude/tools/arch_map.py get booking
.claude/tools/lexicon.py search checkstyle
```

The lexicon is the only part that makes the repository better over time rather
than merely keeping it green. Every entry in it cost real time while this was
being built: that PIT below 1.19 crashes on JDK 21, that a bare
`checkstyle:check` silently runs Sun's ruleset, that `openapi-typescript` breaks
against TypeScript 7.

## The workflow

```
/work gh#42
```

The prompt is an issue number. Everything else is in the repository.

1. **Brief** — `issue_context.py` validates the issue and refuses it if a
   requirement has no scenario, if the requirements are not numbered, or if there
   is no risk tier. Tier 3 is planned and handed to a human.
2. **Plan** — a requirement → scenario → test table, blast radius, an
   architecture check, and if the change touches the wire, the spec is edited
   first and both sides regenerate.
3. **Implement** — `tech-lead`, test first, enforced.
4. **Review** — `reviewer`, read-only, requirements against asserting tests only.
5. **Cycle** — bounded at three rounds, then escalate. A loop that will not
   converge is usually an ambiguous requirement, not a stubborn bug.
6. **Gate** — `./verify --mutation`.

Issues are filed through a form; the form's fields are validated by a script. A
template is a suggestion, a gate is not.

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
