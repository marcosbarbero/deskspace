# 0006. Prefix generated wire types with `Api`

- Status: Accepted
- Date: 2026-02-09
- Supersedes: [0003](0003-wire-types-share-domain-names.md)

## Context

[0003](0003-wire-types-share-domain-names.md) let generated wire types keep the
schema names, so `Desk` existed twice. It survived exactly as long as it took to
write the first class that needed both.

Two things showed up immediately. The mapping code was unreadable, with one
import and one fully qualified name per pair. And an architecture rule that
wanted to say "the domain must not depend on generated wire types" could not be
written clearly, because the rule had to talk about packages rather than about
the types themselves.

The second one is what actually forced this. A boundary you cannot assert is a
boundary you do not have.

## Options considered

### A. Prefix generated types: `ApiDesk`, `ApiBooking` (chosen)

One generator option. Every import says which side of the wire it belongs to, and
the architecture rule reads as a sentence about names rather than a sentence about
directories.

### B. Keep 0003 and rely on package discipline (rejected)

It is what 0003 already said, and it produced a mapping layer nobody wanted to
read within one feature.

### C. Rename the domain types instead (rejected again)

Rejected in 0003 for a reason that has not changed: the domain is the half that
should read naturally.

## Decision

The generator is configured with `modelNamePrefix=Api`. Generated types are
`ApiDesk`, `ApiBooking`, `ApiZone`, `ApiProblem`.

## Consequences

`ArchitectureRulesTest` can now assert that nothing outside a controller depends
on `..api.model..`, and that rule is meaningful rather than incidental.

The prefix is visible in every controller, which is a small ongoing cost paid in
the one layer whose job is translation.

0003 is not deleted. It stays as the record that this was tried the other way,
so the next person proposing it can see it was considered rather than overlooked.
