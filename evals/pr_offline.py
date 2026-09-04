#!/usr/bin/env python3
"""Run pr.py's checks against a fixture ticket instead of a live issue.

The rules are the interesting part and they should be testable without a
network, a token, or a repository that happens to have issue 42 open. This
substitutes the fixture and calls the real check function, so the eval exercises
the shipped logic rather than a copy of it.

    evals/pr_offline.py <pr-body.md> [ticket-fixture-stem]
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "toolbox"))

import pr  # noqa: E402

body_file = Path(sys.argv[1])
stem = sys.argv[2] if len(sys.argv) > 2 else "complete"
ticket = (ROOT / "evals" / "cases" / "tickets" / f"{stem}.md").read_text()

pr.issue = lambda number: {"number": int(number), "state": "OPEN",
                           "title": "fixture", "body": ticket}

problems = pr.check("1", (ROOT / "evals" / body_file).read_text())
if problems:
    print("REFUSED. This pull request body is not reviewable:", file=sys.stderr)
    for p in problems:
        print(f"  - {p}", file=sys.stderr)
    raise SystemExit(1)
print("  ok: the body closes #1, covers every scenario, and cites the gates")
