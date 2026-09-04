# .claude

Everything here is bound to an agent runtime. Anything that is not lives in
[`toolbox/`](../toolbox/README.md), which a person or CI can run just as well.

| | |
|---|---|
| `settings.json` | which commands may run without asking, which files may never be edited, which hooks fire |
| `hooks/` | event handlers: JSON on stdin, an exit code as the answer |
| `commands/flow.md` | `/flow gh#42`, the whole delivery loop behind one word |
| `agents/` | `tech-lead` implements, `reviewer` checks requirements against asserting tests |

## The hooks, and why each one exists

| hook | when | what it removes |
|---|---|---|
| `require_test_first.py` | before an edit | the choice about whether to write the test first |
| `format_after_edit.py` | after an edit | any attention or token spent on formatting |
| `lexicon_recall.py` | on every prompt | having to remember to search for prior lessons |
| `lexicon_capture.py` | at the end of a session | forgetting to write down what was learned |

The first two are gates: they can refuse. The last two are about determinism of a
different kind. Telling an agent "search the lexicon before deriving" is an
instruction, and instructions are followed most of the time; scoring every prompt
against the lexicon and injecting a strong match removes the choice entirely.

Capture is the honest exception. Whether something was surprising is a judgement
no script can make, so that hook only notices that production code changed and
nothing was recorded, and asks while the answer is still fresh. It never blocks.

## The allow list matters as much as the deny list

`permissions.deny` protects the gates: the hooks, `toolbox/verify`,
`ArchitectureRulesTest`, the thresholds. Moving a gate is not passing it.

`permissions.allow` does something different. It names the commands that may run
without a human saying yes, which keeps the set of things that can happen in a
session small enough to reason about. Everything else asks. That is the
difference between an agent that works within a known surface and one whose
execution path is discovered afterwards from a transcript.
