# Where documentation lives

Five kinds of document, five different questions. A document that answers the
wrong question in the wrong place is worse than a missing one, because somebody
will maintain it forever without anyone reading it.

| | question it answers | mutable? | lives in |
|---|---|---|---|
| **PRD** | what are we building, and for whom | yes, until shipped | `docs/prd/` |
| **User journey** | what does a person do, start to finish | yes | `docs/journeys/` |
| **Feature UX** | what does this screen do in every state | yes | `docs/ux/` |
| **ADR** | why is it built this way, and what did we reject | **no, never** | `docs/adr/` |
| **Lexicon** | how did we solve this before | append-only | `docs/lexicon/` |

Three things are deliberately **not** here:

- **API shape** lives in `api/openapi.yaml`. It is generated from, not described
  in prose, so a document about it would be a second source of truth.
- **Architecture rules** live in `ArchitectureRulesTest`. The ADR says why the
  boundary exists; the test is what stops it moving.
- **Conventions an agent needs on every turn** live in `CLAUDE.md`, which is
  loaded into context constantly and therefore stays short. Anything that can be
  looked up on demand belongs in a registry instead.

## The order they get written in

```
PRD          why this is worth building, and what "done" means for the user
  -> journey    what the person actually does, step by step
    -> UX         what each screen shows in every state, including the bad ones
      -> issue      one implementable unit, with numbered requirements and scenarios
        -> ADR        written when a decision was made that closed off alternatives
```

An issue is the only one of these an agent is asked to implement. The rest exist
so the issue can be written well enough to be implementable, which is where most
of the failure actually happens.
