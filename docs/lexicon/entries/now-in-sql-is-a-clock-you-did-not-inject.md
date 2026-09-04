---
key: now-in-sql-is-a-clock-you-did-not-inject
tags: [backend, determinism, sql, testing]
---
# `now()` in SQL is a second clock, and it disagrees with the one you injected

`markPublished` set `published_at = now()`. Every test in this service pins a
`Clock` bean, and none of that reaches the database: the row got the real wall
time.

It only surfaced when retention arrived. The pruner asked "delete rows published
before *fixed now* minus seven days", the rows carried *real* now, and nothing was
ever pruned. The test failed with `expected: 1` and pointed at the pruner, which
was correct.

**Do:** pass the instant in. `markPublished(UUID id, Instant at)` takes it from
the injected clock, so one clock decides what time it is in the whole service.

**Look for the same thing in:** `now()`, `current_timestamp`, `default now()` on
a column, `@CreationTimestamp`, and any trigger that stamps a row. Each is a
clock, and every one of them is invisible to a test that pins yours.

**A second lesson from the same failure.** The first fix made the test pass for
the wrong reason and the test itself was wrong: retention is measured from
*delivery*, not from when the event happened, so a row delivered a minute ago is
kept however old the event is. That is what protects a consumer that has been
slow rather than punishing it. Read a failing assertion twice before deciding
which side of it is wrong.
