#!/usr/bin/env python3
"""Durable memory: how a recurring problem was solved here before.

Gates stop bad things escaping. This stops the same afternoon being spent twice.

    lexicon.py search <words>    entries matching key, title, tags or body
    lexicon.py list              every key with its title
    lexicon.py show <key>        one entry in full
    lexicon.py add --key K --title T [--tags a,b] [--body-file F]
    lexicon.py check             front matter is present and keys match filenames
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ENTRIES = ROOT / "docs" / "lexicon" / "entries"

FRONT = re.compile(r"^---\n(.*?)\n---\n", re.S)


def parse(path: Path) -> dict:
    text = path.read_text()
    meta = {"key": path.stem, "tags": [], "title": "", "body": text, "path": path}
    m = FRONT.match(text)
    if m:
        for line in m.group(1).splitlines():
            if ":" not in line:
                continue
            k, v = line.split(":", 1)
            k, v = k.strip(), v.strip()
            if k == "tags":
                meta["tags"] = [t.strip() for t in v.strip("[]").split(",") if t.strip()]
            elif k in ("key", "title"):
                meta[k] = v
        meta["body"] = text[m.end():]
    if not meta["title"]:
        heading = re.search(r"^#\s+(.*)$", meta["body"], re.M)
        meta["title"] = heading.group(1).strip() if heading else meta["key"]
    return meta


def entries() -> list[dict]:
    return [parse(p) for p in sorted(ENTRIES.glob("*.md"))]


def cmd_search(words: list[str]) -> int:
    needle = " ".join(words).lower()
    hits = [e for e in entries()
            if needle in e["key"].lower() or needle in e["title"].lower()
            or needle in " ".join(e["tags"]).lower() or needle in e["body"].lower()]
    if not hits:
        print(f"  nothing for '{needle}'. If you are about to work it out, add it when you have.")
        return 0
    for e in hits:
        print(f"  {e['key']}\n    {e['title']}\n    tags: {', '.join(e['tags']) or '-'}")
    print(f"\n  {len(hits)} entries. `lexicon.py show <key>` for the whole thing.")
    return 0


def cmd_list() -> int:
    for e in entries():
        print(f"  {e['key']:46}  {e['title']}")
    return 0


def cmd_show(key: str) -> int:
    for e in entries():
        if e["key"] == key:
            print(e["path"].read_text())
            return 0
    print(f"no entry '{key}'", file=sys.stderr)
    return 1


def cmd_add(args) -> int:
    key = args.key.strip().lower().replace(" ", "-")
    path = ENTRIES / f"{key}.md"
    if path.exists():
        print(f"'{key}' already exists. Edit it or pick another key.", file=sys.stderr)
        return 1
    body = Path(args.body_file).read_text() if args.body_file else "\n_TODO: what happened, and what to do instead._\n"
    tags = ", ".join(t.strip() for t in (args.tags or "").split(",") if t.strip())
    path.write_text(f"---\nkey: {key}\ntags: [{tags}]\n---\n# {args.title}\n{body}")
    print(f"  wrote {path.relative_to(ROOT)}")
    return 0


def cmd_check() -> int:
    problems = []
    for path in sorted(ENTRIES.glob("*.md")):
        e = parse(path)
        if not FRONT.match(path.read_text()):
            problems.append(f"{path.name} has no front matter")
        elif e["key"] != path.stem:
            problems.append(f"{path.name} declares key '{e['key']}'")
        if not e["title"]:
            problems.append(f"{path.name} has no title")
    for problem in problems:
        print(f"  FAIL  {problem}")
    if not problems:
        print(f"  lexicon ok ({len(list(ENTRIES.glob('*.md')))} entries)")
    return 1 if problems else 0


def main() -> int:
    ap = argparse.ArgumentParser(add_help=True)
    sub = ap.add_subparsers(dest="cmd")
    s = sub.add_parser("search"); s.add_argument("words", nargs="+")
    sub.add_parser("list")
    sh = sub.add_parser("show"); sh.add_argument("key")
    a = sub.add_parser("add")
    a.add_argument("--key", required=True); a.add_argument("--title", required=True)
    a.add_argument("--tags"); a.add_argument("--body-file")
    sub.add_parser("check")
    args = ap.parse_args()

    if args.cmd == "search":
        return cmd_search(args.words)
    if args.cmd == "show":
        return cmd_show(args.key)
    if args.cmd == "add":
        return cmd_add(args)
    if args.cmd == "check":
        return cmd_check()
    return cmd_list()


if __name__ == "__main__":
    sys.exit(main())
