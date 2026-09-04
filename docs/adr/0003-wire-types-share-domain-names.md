# 0003. Wire types share the domain's type names

- Status: Superseded by [0006](0006-prefix-generated-wire-types.md)
- Date: 2026-02-06

## Context

The generator produces a Java type per schema in the spec. The schemas are named
after the concepts they carry: `Desk`, `Booking`, `Zone`. The domain classes are
named after the same concepts, because they are the same concepts.

## Options considered

### A. Let both use the same simple name (chosen at the time)

`com.marcosbarbero.deskspace.desk.Desk` and
`com.marcosbarbero.deskspace.api.model.Desk`. Java allows it. The reader sees the
distinction in the import.

### B. Rename the domain types (rejected)

`DeskEntity`, `BookingEntity`. Puts the awkwardness on the domain, which is the
half that should read most naturally, to solve a problem created by the other
half.

## Decision

Generated wire types keep the schema names. Where both are needed in one file,
one of them is fully qualified.

## Consequences

Any class that maps between the two has an import of one and a fully qualified
reference to the other, which reads badly but only in the mapping layer, which is
small.

---

*This ADR is kept unchanged. See [0006](0006-prefix-generated-wire-types.md) for
what replaced it and why.*
