# Architecture decision records

An ADR records a decision that closed off alternatives, and it is **immutable**.

## The rule that makes this documentation rather than a journal

**Every ADR must record what was rejected and why.** If every option in the file
was accepted, nothing was decided: you wrote down what you did, which is a
journal entry. The value of an ADR is entirely in the paths not taken, because
those are the ones the next person will otherwise propose again in six months
without knowing they were already considered and rejected for a reason that is
still true.

A set of ADRs where every status is `Accepted` is a smell. Real decisions produce
rejected options, and sometimes an entire rejected ADR.

## Immutability

An ADR is never edited to reflect a change of mind. It is **superseded**: the old
one keeps its text, gains a `Superseded by` line, and the new one explains what
changed. The record of having believed something is part of the record.

Correcting a typo is fine. Rewriting the reasoning is not.

## Statuses

| status | means |
|---|---|
| `Proposed` | written, not yet agreed |
| `Accepted` | in force |
| `Rejected` | considered as a whole and not adopted. Kept, because "why don't we just..." recurs |
| `Superseded by NNNN` | was in force, no longer. Text unchanged |

## Index

| | | status |
|---|---|---|
| [0001](0001-package-by-feature.md) | Package by feature, not by layer | Accepted |
| [0002](0002-the-spec-is-the-contract.md) | The OpenAPI file is the contract, and both sides are generated from it | Accepted |
| [0003](0003-wire-types-share-domain-names.md) | Wire types share the domain's type names | Superseded by 0006 |
| [0004](0004-a-shared-types-package.md) | A shared TypeScript/Java types package | **Rejected** |
| [0005](0005-in-memory-persistence.md) | In-memory persistence for the reference implementation | Accepted |
| [0006](0006-prefix-generated-wire-types.md) | Prefix generated wire types with `Api` | Accepted |
| [0007](0007-contract-tests-alongside-the-spec.md) | Consumer-driven contract tests alongside the spec | Accepted |

## Template

```markdown
# NNNN. <decision, as a sentence>

- Status: Proposed | Accepted | Rejected | Superseded by NNNN
- Date: YYYY-MM-DD

## Context

What is true that forces a decision. No solutions here.

## Options considered

### A. <option>  (chosen | rejected)
What it is, and the consequence that decided it.

### B. <option>  (rejected)
...

At least two, and at least one rejected. One option is not a decision.

## Decision

The chosen option, in one sentence, in the present tense.

## Consequences

What becomes easier, what becomes harder, and what is now expensive to reverse.
```
