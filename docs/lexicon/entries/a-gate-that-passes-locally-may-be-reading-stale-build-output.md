---
key: a-gate-that-passes-locally-may-be-reading-stale-build-output
tags: [build, ci, coverage, determinism]
---
# A gate that passes locally may be reading stale build output

The coverage gate passed on my machine and failed in CI, on the same commit.

`JdbcBookings` is only exercised by tests that need Docker, and those are
excluded from the default build. Locally, `target/jacoco.exec` still held data
from an earlier `-Pdatabase` run, so the default build's coverage check counted
lines that the default build had not executed. CI starts from an empty checkout
and counted the truth.

**Do two things.** Make the coverage exclusions match what the running suite can
actually cover: the default build excludes the adapter it cannot reach, and the
database profile drops that exclusion because that run does reach it. And when a
gate disagrees between your machine and CI, delete `target/` before believing
either one.

**The general form:** a build directory is ambient state, exactly like a clock or
a working tree, and any gate that reads accumulated output is only as
reproducible as the last person's local history. CI is not stricter than your
machine here. It is just cleaner.

## It happened again two hours later

Writing this entry did not prevent the second occurrence, which is the more
useful half of the story. Same gate, same cause, new classes, and I did not think
of it because the lesson was something to remember and nothing was checking.

Two things changed after that:

- `toolbox/verify` now runs `clean verify`. Slower, and local and CI now mean the
  same thing, which is the only property that matters in a gate.
- `toolbox/check_coverage_exclusions.py` refuses an exclusion for a class that is
  not database-only, so the list cannot quietly start hiding covered code.

**The real lesson is the meta one.** A lexicon entry is a good way to stop paying
for a *diagnosis* twice. It is a poor way to stop repeating a *mistake*, because
it still relies on somebody remembering at the right moment. When a lesson is
about something a script could check, write the script; the entry is then a
record of why the script exists.
