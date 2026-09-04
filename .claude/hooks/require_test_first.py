#!/usr/bin/env python3
"""PreToolUse: refuse production code before a test exists. Both sides of the wire.

Asking an assistant to work test-first gets compliance most of the time.
Exit code 2 gets it every time.

Allowed when any of these holds:
  - the target is not production source (tests, docs, config, the spec itself)
  - the branch already has a new or changed test on the *same side* of the wire
  - HARNESS_SKIP_TEST_FIRST=1 (documented, loud, and not a habit)

The per-side rule matters here. A React test does not make it safe to write
unasserted Java, and a Java test does not cover a component. Most single-service
harnesses get this wrong the moment a second deployable appears.

Exit 0 = allow. Exit 2 = block, and say why.
"""
from __future__ import annotations

import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from hook_io import git, repo_root, target_path  # noqa: E402

# production prefix -> (human name, test globs that count for it)
SIDES = {
    "backend/src/main/java/": ("backend", ("backend/src/test/",)),
    "frontend/src/": ("frontend", (".test.ts", ".test.tsx", ".pact.test.ts")),
}

BLOCKED = """\
BLOCKED by the harness: test-first, {side}.

You are editing {target}
and this branch has no new or changed {side} test.

Write the failing test first, then implement. The test is the specification: if
you cannot express the requirement as a failing test, the requirement is not yet
clear enough to implement.

A test on the other side of the wire does not count. A component test does not
prove a booking rule, and a service test does not prove a screen.

This is a gate, not a preference. Editing this hook is denied in
.claude/settings.json.
"""


def side_for(target: str):
    for prefix, (name, markers) in SIDES.items():
        if prefix in target:
            if "/src/test/" in target or any(m in target for m in (".test.ts", ".test.tsx")):
                return None  # editing a test is always allowed
            return name, prefix, markers
    return None


def changed_files(root: Path) -> set[str]:
    changed: set[str] = set()
    for args in (("diff", "--name-only"), ("diff", "--cached", "--name-only"),
                 ("ls-files", "--others", "--exclude-standard")):
        changed |= {line for line in git(*args, cwd=root).splitlines() if line.strip()}
    for base in ("main", "master"):
        if git("rev-parse", "--verify", "--quiet", base, cwd=root).strip():
            head = git("rev-parse", "HEAD", cwd=root).strip()
            if head and head != git("rev-parse", base, cwd=root).strip():
                changed |= {l for l in git("diff", "--name-only", f"{base}...HEAD", cwd=root).splitlines() if l.strip()}
            break
    return changed


def main() -> int:
    target = target_path()
    resolved = side_for(target)
    if resolved is None:
        return 0
    side, _prefix, markers = resolved

    if os.environ.get("HARNESS_SKIP_TEST_FIRST") == "1":
        print(f"require-test-first: bypassed for {side} via HARNESS_SKIP_TEST_FIRST=1", file=sys.stderr)
        return 0

    root = repo_root()
    if root is None:
        return 0

    for path in changed_files(root):
        if any(marker in path for marker in markers):
            return 0

    print(BLOCKED.format(side=side, target=target), file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main())
