#!/usr/bin/env python3
"""Every directory a person can land in has a README.

A repository is navigated by opening a folder and looking. A folder with no
README answers nothing, and the reader either guesses or asks, and both are worse
than three sentences written once.

    toolbox/check_readmes.py
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

# Directories nobody navigates to: build output, dependencies, generated trees,
# and leaf packages whose parent already explains the layout.
IGNORE_NAMES = {".git", "node_modules", "target", "dist", ".mvn", "wrapper",
                ".github", "entries", "cases", "tickets", "prs", "hooks",
                "agents", "commands", "adr", "prd", "journeys", "ux",
                "architecture", "checkstyle", "static", "templates", "db",
                "messages", "fragments", "resources", "scss", "jmeter"}


def interesting(path: Path) -> bool:
    parts = path.relative_to(ROOT).parts
    if any(p in IGNORE_NAMES or p.startswith(".") and p != ".claude" for p in parts):
        return False
    # source trees are explained by their module's README, not folder by folder
    return not any(p in {"src", "main", "java", "test", "com", "marcosbarbero",
                         "deskspace", "features", "api", "booking", "desk",
                         "availability", "shared", "contract", "lexicon"}
                   for p in parts[1:])


def main() -> int:
    missing = []
    for path in sorted(ROOT.iterdir()):
        if not path.is_dir() or not interesting(path):
            continue
        if not (path / "README.md").exists():
            missing.append(path.relative_to(ROOT))
    for m in missing:
        print(f"  FAIL  {m}/ has no README.md: a folder that answers nothing "
              f"costs every reader the same question")
    if not missing:
        count = sum(1 for p in ROOT.iterdir() if p.is_dir() and interesting(p))
        print(f"  readmes ok ({count} directories, all documented)")
    return 1 if missing else 0


if __name__ == "__main__":
    sys.exit(main())
