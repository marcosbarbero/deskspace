#!/usr/bin/env python3
"""Run a hook against a scratch repository whose contents this script decides.

The test-first hook answers from the state of a git working tree. Running it
against the developer's own tree means the answer depends on what they happen to
have uncommitted, which is the opposite of what an eval is for.

    evals/hook_offline.py <target-path> [--with backend-test|frontend-test]

Exits with the hook's exit code.
"""
from __future__ import annotations

import os
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HOOK = ROOT / ".claude" / "hooks" / "require_test_first.py"

MARKERS = {
    "backend-test": "backend/src/test/java/com/example/SomethingTest.java",
    "frontend-test": "frontend/src/features/booking/DeskBoard.test.tsx",
}


def main() -> int:
    target = sys.argv[1]
    with_marker = sys.argv[sys.argv.index("--with") + 1] if "--with" in sys.argv else None

    with tempfile.TemporaryDirectory() as tmp:
        repo = Path(tmp)
        subprocess.run(["git", "init", "-q"], cwd=repo, check=True)
        if with_marker:
            marker = repo / MARKERS[with_marker]
            marker.parent.mkdir(parents=True, exist_ok=True)
            marker.write_text("// a test exists on this branch\n")

        done = subprocess.run(
            ["python3", str(HOOK)],
            input='{"tool_input": {"file_path": "%s"}}' % target,
            text=True, capture_output=True,
            env={**os.environ, "HARNESS_REPO_ROOT": str(repo)},
        )
        sys.stderr.write(done.stderr)
        sys.stdout.write(done.stdout)
        return done.returncode


if __name__ == "__main__":
    raise SystemExit(main())
