---
key: a-drift-gate-must-compare-to-a-snapshot-not-to-head
tags: [tooling, gates, codegen]
---
# A "generated file is current" gate must compare to a snapshot, not to HEAD

The gate regenerated the client and then ran `git diff --quiet` on it. That asks
"has this file changed since the last commit", which is not the question. The
question is "does regenerating change it".

The two agree exactly while you have no uncommitted spec change, which is to say
they agree until the moment the gate would be useful. The first real spec change,
correctly regenerated and not yet committed, failed the build.

**Do:** snapshot the file, regenerate, diff against the snapshot, restore
nothing. `toolbox/verify`'s `spec_is_current` does this.

**How it was found:** by using the workflow to implement a feature that changed
the spec. No amount of reading the gate would have shown it, because on a clean
tree it behaves identically.

**The general form:** a check about a *process* (regeneration is a no-op) must
not be implemented as a check about *history* (nothing changed since a commit).
