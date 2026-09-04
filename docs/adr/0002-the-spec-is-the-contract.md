# 0002. The OpenAPI file is the contract, and both sides are generated from it

- Status: Accepted
- Date: 2026-02-06

## Context

Two deployables have to agree about a wire format. They are written in different
languages, built by different tools, and changed by different people, sometimes
in the same afternoon.

The failure is not that somebody writes the wrong field name. It is that both
sides are individually correct, tested, green, and disagree, and nothing in
either build has any way to notice.

## Options considered

### A. One OpenAPI document, both sides generated from it (chosen)

`api/openapi.yaml` is the source. The backend generates its controller interfaces
and wire types; the front end generates its types and client. Neither can name a
field the spec does not have, because the code that names it does not compile.

### B. Hand-written types on both sides, kept in step by review (rejected)

Works until the first Friday. The check is a human reading two diffs in two
languages and noticing that `bookedBy` became `booked_by` in one of them. That is
exactly the class of check a person does badly and a compiler does perfectly.

### C. Backend-first: generate the spec from the Java code (rejected)

Tempting, and it inverts the dependency the wrong way. The spec becomes a
description of whatever the backend happens to do, so it can never refuse a
change: any breaking change to the API produces a new, equally valid spec. The
contract has to be able to say no, which means it cannot be derived from one of
the parties.

### D. Contract tests only, no shared document (rejected as the whole answer)

Consumer-driven contract tests catch real drift, and they are adopted here for
that reason. What they do not give you is a single artifact to point at when
somebody asks what the API is. Both, not either.

## Decision

`api/openapi.yaml` is the contract. Generated code is written to build output and
never committed, so there is no version of it to edit by hand.

## Consequences

A change to the API starts by editing the spec, which is a slower and more
deliberate first step than editing a controller. That is the intended cost.

Generated code cannot be adjusted when it is inconvenient. When the generator
produces something awkward, the options are to change the spec or change the
generator configuration, both of which are visible to everyone.

The spec becomes a merge-conflict hotspot for changes that touch both sides.
Accepted: a conflict in one file is cheaper than silent disagreement in two.
