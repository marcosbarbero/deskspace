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
| backend codegen | `openapi-generator` produces interfaces and `Api*` types into `target/` |
| backend slices | `desk`, `booking`, `availability`, `shared` |
| `ArchitectureRulesTest` | 5 ArchUnit rules: slice isolation, no cycles, no field injection, domain free of wire types |
| deterministic tests | injected `Clock` and id supplier, so no test depends on the wall clock |

Still to come: the front end and its generated client, consumer-driven contract
tests, coverage and mutation thresholds, the pre-push gate, issue forms, the
workflow commands, and evals.

## Running it

```bash
cd backend
./mvnw test            # unit tests plus the architecture rules
./mvnw spring-boot:run # http://localhost:8080
```
