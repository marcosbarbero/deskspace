# 0001. Package by feature, not by layer

- Status: Accepted
- Date: 2026-02-04

## Context

Every class in this service belongs to one of two groupings: what it *is*
(controller, service, repository) or what it is *about* (desks, bookings,
availability). Only one of them can be the package structure.

The choice matters more than usual here because an agent is asked to work in this
repository from an issue. Whatever the packages say is where it will look, and
whatever the boundaries allow is what it will couple together without being told.

## Options considered

### A. Package by feature (chosen)

`desk`, `booking`, `availability`, `shared`. A change to booking rules touches one
directory. Dependencies between features are visible as package dependencies, so
they can be asserted.

### B. Package by layer (rejected)

`controller`, `service`, `repository`, `model`. Familiar, and every change touches
four directories. The blast radius of a feature is unbounded by construction,
which makes "what does this change affect" unanswerable without reading it.
Nothing about the arrangement can be asserted, because every class legitimately
depends on the layer below it.

### C. Package by feature with a shared `model` package (rejected)

The compromise most teams land on. It fails for the reason it is proposed: the
shared model becomes the place every feature reaches into, so the boundaries stop
meaning anything within a release or two, and no test can tell you when that
happened.

## Decision

Packages are features. `desk` owns the catalogue, `booking` owns booking rules,
`availability` composes the two, `shared` holds only cross-cutting configuration
with no domain content.

## Consequences

`desk` must not know about bookings, which means desk availability cannot live in
`desk`. That is what `availability` exists for, and it is the awkward part of this
decision: a newcomer's first instinct is to put `available` on `Desk`.

`ArchitectureRulesTest` asserts the direction of every edge, so the awkwardness is
enforced rather than remembered. Reversing this decision means rewriting those
rules, which is deliberately visible in a diff.
