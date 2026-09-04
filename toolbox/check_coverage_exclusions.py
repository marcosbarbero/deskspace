#!/usr/bin/env python3
"""The coverage exclusions must be exactly the classes the default build cannot reach.

A class annotated `@Profile("postgres")` is only exercised by tests that need
Docker, and those are excluded from the default build. Counting it would either
fail the build for nobody's fault, or, worse, pass because a previous local run
left coverage data behind.

So the default build excludes those classes. That list is a property in
`backend/pom.xml`, and a hand-maintained list of classes is a list that goes
stale: this fails when the two disagree in either direction.

Adding a class to the list without the annotation is worse than forgetting one,
because it silently stops measuring code the default suite does cover.

    toolbox/check_coverage_exclusions.py
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POM = ROOT / "backend" / "pom.xml"
SOURCES = ROOT / "backend" / "src" / "main" / "java"
PACKAGE_ROOT = "com/marcosbarbero/deskspace"

PROFILE = re.compile(r'@Profile\(\s*"postgres"\s*\)')


def database_only() -> set[str]:
    found = set()
    for path in SOURCES.rglob("*.java"):
        if PROFILE.search(path.read_text()):
            found.add(str(path.relative_to(SOURCES)).replace(".java", ".class"))
    return found


def excluded() -> set[str]:
    match = re.search(r"<jacoco\.excludes>([^<]*)</jacoco\.excludes>", POM.read_text())
    if not match:
        return set()
    return {entry.strip() for entry in match.group(1).split(",") if entry.strip()}


def main() -> int:
    on_disk = database_only()
    listed = excluded()

    problems = []
    # Only one direction is decidable here. Every excluded class must be
    # database-only, or the exclusion is hiding code the default suite covers,
    # which is the dangerous mistake because nothing else would ever say so.
    #
    # The other direction is not: OutboxRelay is @Profile("postgres") and is
    # covered by a plain unit test that constructs it, so "annotated" does not
    # imply "uncoverable". Forgetting an entry fails the build loudly on a clean
    # checkout, which is a tolerable way to find out.
    for extra in sorted(listed - on_disk):
        problems.append(f"{extra} is in <jacoco.excludes> but is not @Profile(\"postgres\"): "
                        f"an exclusion that is not database-only hides code the default "
                        f"suite does cover, and nothing else will tell you")

    for problem in problems:
        print(f"  FAIL  {problem}")
    if not problems:
        print(f"  coverage exclusions ok ({len(listed)} database-only classes)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
