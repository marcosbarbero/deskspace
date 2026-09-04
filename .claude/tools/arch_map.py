#!/usr/bin/env python3
"""Where the code lives, as data rather than as something to be inferred.

Without this, "add validation to the booking date" starts with a tree walk and a
handful of file opens to rediscover a structure that has not changed since the
repository was created, and rediscovers it again next session. The structure is
a fact about the repo, so it lives in a file.

    arch_map.py list           slices and what each is responsible for
    arch_map.py get <slice>    paths, owned classes, allowed dependencies
    arch_map.py check          the map against the tree; fails on drift
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MAP = ROOT / "docs" / "architecture" / "mapping.json"


def load() -> dict:
    return json.loads(MAP.read_text())


def cmd_list() -> int:
    slices = load()["slices"]
    width = max(len(k) for k in slices)
    for name, meta in slices.items():
        print(f"  {name:{width}}  {meta['responsibility']}")
    return 0


def cmd_get(name: str) -> int:
    data = load()
    slices = data["slices"]
    if name not in slices:
        print(f"no slice '{name}'. Known: {', '.join(slices)}", file=sys.stderr)
        return 1
    meta = slices[name]
    print(f"  {name}\n")
    print(f"  responsibility  {meta['responsibility']}")
    for path in meta["paths"]:
        print(f"  path            {path}")
    print(f"  owns            {', '.join(meta['owns'])}")
    allowed = ", ".join(meta["may_depend_on"]) or "nothing"
    print(f"  may depend on   {allowed}")
    print(f"\n  enforced by     {data['enforced_by']}")
    return 0


def cmd_check() -> int:
    data = load()
    problems = []
    mapped: set[Path] = set()
    for name, meta in data["slices"].items():
        for rel in meta["paths"]:
            path = ROOT / rel
            mapped.add(path)
            if not path.is_dir():
                problems.append(f"slice '{name}' maps {rel}, which is not a directory")
        for dep in meta["may_depend_on"]:
            if dep not in data["slices"]:
                problems.append(f"slice '{name}' may_depend_on '{dep}', which is not a slice")

    # A package on disk that the map does not know about sends the next reader,
    # or the next agent, confidently to the wrong place.
    for base in (ROOT / "backend/src/main/java/com/marcosbarbero/deskspace",
                 ROOT / "frontend/src/features"):
        if not base.is_dir():
            continue
        for child in sorted(base.iterdir()):
            if child.is_dir() and child not in mapped:
                problems.append(f"{child.relative_to(ROOT)} exists but no slice claims it")

    if not (ROOT / data["enforced_by"]).exists():
        problems.append(f"enforced_by points at {data['enforced_by']}, which is not in the repo")

    for problem in problems:
        print(f"  FAIL  {problem}")
    if not problems:
        print(f"  architecture map ok ({len(data['slices'])} slices)")
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
