---
key: an-eval-that-reads-your-working-tree-is-not-an-eval
tags: [evals, determinism, tooling]
---
# An eval that reads your working tree is not an eval

The test-first hook answers from git: it allows an edit when the branch already
has a test change. The eval case ran the hook directly, so its verdict depended
on what happened to be uncommitted, and it went red in the middle of a refactor
that had legitimately added tests. The gate was right and the eval was wrong.

**Do:** give the hook a scratch repository whose contents the case decides.
`hook_io.repo_root()` honours `HARNESS_REPO_ROOT`, and `evals/hook_offline.py`
creates a temporary repo per case.

**The general form:** a check that reads ambient state has a source of
non-determinism, exactly like a clock. Make it injectable, for the same reason
and by the same means. The backend already does this with `Clock` and the id
supplier; the hooks had not caught up.
