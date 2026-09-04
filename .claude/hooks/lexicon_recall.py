#!/usr/bin/env python3
"""UserPromptSubmit: inject what we already learned, without being asked.

This is what makes a lexicon more than a directory of files.

"Search the lexicon before deriving" is an instruction, and instructions are
followed most of the time. This removes the choice: every prompt is scored
against the lexicon and a strong match is injected before the model sees the
task. Nobody has to remember, because nobody is asked.

Deliberately quiet. It says nothing unless a match is strong, and never more
than three entries. A memory that fires on every turn is a banner, and banners
are skimmed past.

Always exits 0. Recall that can block a prompt is recall that gets removed.
"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from hook_io import payload, repo_root  # noqa: E402


def main() -> int:
    prompt = (payload().get("prompt") or "").strip()
    if len(prompt) < 12:
        return 0
    root = repo_root()
    if root is None:
        return 0
    try:
        done = subprocess.run(["toolbox/lexicon.py", "recall", prompt, "--limit", "3",
                               "--threshold", "5"],
                              cwd=root, capture_output=True, text=True, timeout=15)
    except (OSError, subprocess.SubprocessError):
        return 0
    if done.returncode == 0 and done.stdout.strip():
        # stdout from a UserPromptSubmit hook becomes context for this turn.
        print(done.stdout)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
