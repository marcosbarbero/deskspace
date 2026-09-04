---
description: Implement an issue end to end — brief, plan, TDD, review, gates, PR
argument-hint: gh#42 (or just 42)
---

Implement issue **$ARGUMENTS** end to end.

The prompt is an issue number and nothing else. Everything you need is in this
repo: the issue carries the requirements and scenarios, `CLAUDE.md` carries the
conventions, the registries carry the map, and `./verify` carries the definition
of done. If something is missing, that is a defect in the ticket. Say so and stop.

**Deterministic first, agent on failure.** Every step a script can do, a script
does. You are used where judgement is genuinely required, which is fewer places
than it feels like.

---

## 1 · Brief (no judgement)

```bash
.claude/tools/issue_context.py $ARGUMENTS
```

- **exit 1** → not implementable. Print the reasons and **stop.** Do not repair
  the ticket yourself.
- **exit 2** → tier 3. Produce the plan from step 2, then **stop and wait for a
  human.**
- **exit 0** → continue. The brief is now your source of truth; prefer it over
  re-reading the issue.

Then look up the area rather than searching for it, and check whether this has
been solved before:

```bash
.claude/tools/arch_map.py get <area from the issue>
.claude/tools/lexicon.py search "<the thing that looks unfamiliar>"
git switch -c issue-<number>
```

## 2 · Plan, and decide the seam

Before touching code, produce:

- the **Outcome**, restated in one sentence
- a **requirement → scenario → test** table, one row per requirement. If a
  requirement has no scenario covering it, **stop and say so.** That is a gap in
  the ticket, not something to improvise around.
- the **blast radius**: every file you expect to touch
- an **architecture check**: does this cross a slice boundary? `desk` may not
  know about `booking`. Anything into `shared` that carries domain meaning is
  wrong. If it crosses, stop and say so.
- **does this touch the wire?** If any requirement changes a request, a response
  or a status code, then the order is fixed:

  1. edit `api/openapi.yaml` first
  2. regenerate both sides (`cd frontend && npm run gen`; the backend regenerates
     on build)
  3. make the backend satisfy the new interface
  4. make the front end compile against the new types
  5. update `pacts/` only if what the front end *depends on* changed

  Doing this in any other order produces a green build on one side and a broken
  one on the other, which is precisely what the contract exists to prevent.

**Break down if the brief needs it.** If the scenarios describe more than one
behaviour, do them one at a time, fully, red to green to reviewed, before
starting the next. Sequential, not parallel.

## 3 · Implement (delegate to `tech-lead`)

Dispatch the **tech-lead** subagent with the brief and the plan for the current
unit.

Test-first is not a request. `.claude/hooks/require_test_first.py` rejects any
edit to `backend/src/main/java` or `frontend/src` on a branch with no test change
**on that side of the wire**. A React test does not unlock Java. If the tech lead
reports being blocked, it skipped a step; send it back rather than working around
the hook.

## 4 · Review (delegate to `reviewer`)

Dispatch the **reviewer** subagent with the brief and the diff. Its only question
is whether the numbered requirements are satisfied, as proven by the scenarios,
as proven by asserting tests. Formatting, architecture, coverage and mutation are
already decided by gates; re-reviewing them is the reviewer-fatigue anti-pattern
this repository exists to remove.

It returns `APPROVED` or `CHANGES REQUESTED` with numbered defects.

## 5 · The cycle

Send defects back, review again. **Bounded at three rounds.** After that, stop
and escalate with: what still fails, which scenario it belongs to, and what you
think the ticket got wrong. A loop that will not converge is nearly always an
ambiguous requirement, not a stubborn bug.

## 6 · Gate

```bash
./verify --mutation
```

Green, or you are not done. Never weaken a test or a threshold to get there.

## 7 · Learn, then push

If anything surprised you, record it before you finish:

```bash
.claude/tools/lexicon.py add --key <key> --title "<what to know>" --tags <area>
```

Then push. The pre-push hook runs `./verify` again. There is no bypass, and
`--no-verify` is denied.

---

## Do not

- Fill in a missing requirement by guessing. Refuse the ticket.
- Touch `.githooks/`, `.claude/hooks/`, `verify`, `ArchitectureRulesTest`, or a
  threshold. Moving a gate is not passing it.
- Hand-write anything under `frontend/src/api/schema.d.ts` or `backend/target/`.
- Implement anything the brief did not ask for.
- Report success while any gate is red.
