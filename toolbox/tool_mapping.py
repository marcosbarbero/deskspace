#!/usr/bin/env python3
"""The tool registry: what exists, looked up by key.

Listing these in CLAUDE.md would also work, and that file is loaded on every
turn, so the list would cost tokens forever and still have to be re-read. A
registry is looked up once and recalled by key.

    tool_mapping.py list          keys and one-line summaries
    tool_mapping.py get <key>     detail for one
    tool_mapping.py check         fails if a tool exists on disk but is not here
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MAPPING = Path(__file__).parent / "mapping.json"
SKIP = {"tool_mapping.py", "__init__.py"}


def load() -> dict:
    return json.loads(MAPPING.read_text())["tools"]


def cmd_list() -> int:
    tools = load()
    width = max(len(k) for k in tools)
    for key, meta in sorted(tools.items()):
        print(f"  {key:{width}}  {meta['summary']}")
    print(f"\n  {len(tools)} tools. `tool_mapping.py get <key>` for detail.")
    return 0


def cmd_get(key: str) -> int:
    tools = load()
    if key not in tools:
        print(f"no tool '{key}'. Known: {', '.join(sorted(tools))}", file=sys.stderr)
        return 1
    meta = tools[key]
    print(f"  {key}\n")
    for field in ("summary", "use_when", "replaces", "path", "example"):
        if meta.get(field):
            print(f"  {field:9} {meta[field]}")
    return 0


def cmd_check() -> int:
    tools = load()
    registered = {Path(m["path"]).name for m in tools.values()}
    on_disk = {p.name for p in (ROOT / "toolbox").glob("*.py") if p.name not in SKIP}
    problems = []
    for name in sorted(on_disk - registered):
        problems.append(f"{name} exists but is not registered in mapping.json: "
                        f"an unregistered tool is an undiscoverable tool")
    for key, meta in sorted(tools.items()):
        if not (ROOT / meta["path"]).exists():
            problems.append(f"'{key}' points at {meta['path']}, which is not in the repo")
    for problem in problems:
        print(f"  FAIL  {problem}")
    if not problems:
        print(f"  tool registry ok ({len(tools)} tools)")
    return 1 if problems else 0


def main() -> int:
    args = sys.argv[1:]
    if not args or args[0] == "list":
        return cmd_list()
    if args[0] == "get" and len(args) > 1:
        return cmd_get(args[1])
    if args[0] == "check":
        return cmd_check()
    print(__doc__, file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())
