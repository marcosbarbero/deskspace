# Evals

The gates in this repository decide whether a change is done. They are code, and
untested code that has authority is the worst kind. So the gates are tested here.

Two suites, and they are different things:

| | what it asks | deterministic | runs in `./verify` |
|---|---|---|---|
| `harness` | does the gate say yes and no in the right places | yes | yes |
| `agent` | does the model do the right thing when the gate says no | no | no, opt in |

## Why the harness suite exists

A gate that has never been shown to *reject* anything is a gate you are trusting
on faith. Every case here is written the same way: an input, the verdict the gate
must return, and the reason. Half of them assert a refusal, because that is the
half that matters and the half that silently stops working.

```bash
evals/run.py            # the harness suite, about a second
evals/run.py --verbose  # with the output of each case
```

Each case names the gate it exercises. When a gate changes, its cases change in
the same commit, and a gate with no case is reported.

## Why the agent suite is separate

Whether a model refuses an underspecified ticket instead of guessing is a real
question and not a deterministic one. Those cases live in `cases/tickets/` as
fixtures a person or a scheduled run can feed to the workflow, with the expected
behaviour written down. They are not part of `./verify`, because a non-repeatable
check in a blocking gate teaches people to re-run red builds.

The deterministic suite is what protects the deterministic gates. The agent suite
is evidence about the parts no gate can cover.
