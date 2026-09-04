"""Shared plumbing for the hooks: read the tool call, find the repo, run git."""
from __future__ import annotations

import json
import subprocess
import sys
from functools import lru_cache
from pathlib import Path


def payload() -> dict:
    try:
        return json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        return {}


def target_path(data: dict | None = None) -> str:
    """The file a Write/Edit tool call is about, as an absolute-ish string."""
    data = payload() if data is None else data
    tool_input = data.get("tool_input") or {}
    for key in ("file_path", "path", "notebook_path"):
        value = tool_input.get(key)
        if isinstance(value, str) and value:
            return value
    return ""


@lru_cache(maxsize=1)
def repo_root() -> Path | None:
    out = git("rev-parse", "--show-toplevel", cwd=Path(__file__).resolve().parent)
    return Path(out.strip()) if out.strip() else None


def git(*args: str, cwd: Path | None = None) -> str:
    try:
        done = subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True, timeout=10)
    except (OSError, subprocess.SubprocessError):
        return ""
    return done.stdout if done.returncode == 0 else ""
