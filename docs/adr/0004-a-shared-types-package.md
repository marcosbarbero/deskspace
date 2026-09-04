# 0004. A shared TypeScript/Java types package

- Status: **Rejected**
- Date: 2026-02-07

## Context

Recorded because this is the first thing proposed whenever somebody sees two
codebases with the same concepts in them, and it will be proposed again.

The idea: publish the wire types as a versioned package that both sides depend
on, so there is one definition rather than two generated copies.

## Options considered

### A. A published shared package (rejected)

It creates a third artifact with its own release cycle, sitting between two
things that already have to agree. Now a change to the API needs a package
release, a version bump on each side, and two merges, in an order. During the
window between them, the two sides disagree and the package says they do not.

It also cannot be done once. The Java types and the TypeScript types are
different artifacts however they are produced, so "one definition" means one
*source*, which the spec already is.

### B. Generate from the spec on both sides at build time (chosen instead)

No third artifact, no release cycle, no window. The generated code is rebuilt on
every build from a file that lives in the same commit as the code using it, so
disagreement is not representable: they are always generated from whatever the
current commit says.

### C. A monorepo package built from source, not published (rejected)

Removes the release cycle and keeps the coupling. But it either wraps the
generated types, in which case it is a layer with no content, or it replaces
them, in which case it is hand-written and [0002](0002-the-spec-is-the-contract.md)
already rejected that.

## Decision

There is no shared types package. Both sides generate from `api/openapi.yaml` at
build time.

## Consequences

Neither side can depend on the other's types at compile time, which is correct:
they are separate deployables and a compile-time dependency between them would be
a lie about how they are released.

The two generated shapes are not identical. Java gets `ApiDesk` with getters,
TypeScript gets a structural type. Anyone expecting one definition will be
briefly disappointed, and then will notice that they never actually needed one.
