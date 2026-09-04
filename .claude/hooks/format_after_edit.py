#!/usr/bin/env python3
"""PostToolUse: format what was just edited, so nobody spends attention on it.

Java goes through spring-javaformat, TypeScript through eslint --fix. Both are
the same tools toolbox/verify checks with, so formatting can never be a surprise at
push time.

Always exits 0. A formatter that can block an edit is a formatter that will be
turned off.
"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from hook_io import repo_root, target_path  # noqa: E402


def run(cmd: list[str], cwd: Path) -> None:
    try:
        subprocess.run(cmd, cwd=cwd, capture_output=True, timeout=180)
    except (OSError, subprocess.SubprocessError):
        pass


def main() -> int:
    target = target_path()
    root = repo_root()
    if root is None or not target:
        return 0

    if target.endswith(".java") and "backend/" in target:
        run(["./mvnw", "-q", "-B", "spring-javaformat:apply"], root / "backend")
    elif target.endswith((".ts", ".tsx")) and "frontend/" in target:
        rel = target.split("frontend/", 1)[1]
        run(["npx", "eslint", "--fix", rel], root / "frontend")
    return 0


if __name__ == "__main__":
    sys.exit(main())
