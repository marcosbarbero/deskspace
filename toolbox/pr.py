#!/usr/bin/env python3
"""Open a pull request, or refuse to.

`gh pr create` is denied in .claude/settings.json. Not as a formality: this
checks things a reviewer cannot reasonably check by eye at six in the evening.

    pr.py create --issue 42 --body-file draft.md [--dry-run]
    pr.py check  --issue 42 --body-file draft.md

Refuses when the body does not close an open issue, is missing a required
section, omits any of the issue's scenarios from its coverage table, carries no
evidence that the gates ran, or the issue is tier 3.

Exit 0 = opened (or would open). Exit 1 = refused, with reasons.
"""
from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REQUIRED = ("What changed", "Requirements covered", "Evidence")


def gh(*args: str) -> tuple[int, str, str]:
    done = subprocess.run(["gh", *args], cwd=ROOT, capture_output=True, text=True)
    return done.returncode, done.stdout, done.stderr


def issue(number: str) -> dict:
    code, out, err = gh("issue", "view", number, "--json", "number,title,body,state")
    if code != 0:
        print(f"could not read issue {number}: {err.strip()}", file=sys.stderr)
        raise SystemExit(1)
    import json
    return json.loads(out)


def scenarios(text: str) -> list[str]:
    return [re.sub(r"\s+", " ", m.group(1)).strip().lower()
            for m in re.finditer(r"^\s*(?:[-*]\s*)?(?:\*\*)?Scenario:\s*(.+?)\s*$", text, re.M | re.I)]


def check(number: str, body: str) -> list[str]:
    problems: list[str] = []
    data = issue(number)

    if data["state"] != "OPEN":
        problems.append(f"issue #{number} is {data['state'].lower()}: a pull request closes open work")

    if not re.search(rf"\b(closes|fixes|resolves)\s+#{number}\b", body, re.I):
        problems.append(f"the body does not say it closes #{number}, so merging leaves the issue open")

    for section in REQUIRED:
        if not re.search(rf"^#+\s*{re.escape(section)}", body, re.M | re.I):
            problems.append(f"no '{section}' section")

    if re.search(r"tier\s*[:\-]?\s*3", data["body"] or "", re.I):
        problems.append("issue is tier 3: high-risk work is opened by a human, not by a tool")

    # Nobody cross-references four Gherkin blocks against a markdown table at 6pm.
    body_low = body.lower()
    for scenario in scenarios(data["body"] or ""):
        head = " ".join(scenario.split()[:5])
        if head and head not in body_low:
            problems.append(f"scenario not in the coverage table: '{scenario[:64]}'")

    if not re.search(r"(verify|mutation|coverage)", body, re.I):
        problems.append("no evidence that the gates ran: name the command and its result")

    return problems


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("action", choices=("create", "check"))
    ap.add_argument("--issue", required=True)
    ap.add_argument("--body-file", required=True)
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    number = args.issue.lstrip("gh#")
    body = Path(args.body_file).read_text()
    problems = check(number, body)

    if problems:
        print("REFUSED. This pull request body is not reviewable:", file=sys.stderr)
        for p in problems:
            print(f"  - {p}", file=sys.stderr)
        print("\nThe gates already proved the code works. This proves a human can review "
              "the intent without reading the diff.", file=sys.stderr)
        return 1

    if args.action == "check" or args.dry_run:
        print(f"  ok: the body closes #{number}, covers every scenario, and cites the gates")
        return 0

    data = issue(number)
    code, out, err = gh("pr", "create", "--title", data["title"], "--body-file", args.body_file)
    if code != 0:
        print(err.strip(), file=sys.stderr)
        return 1
    print(out.strip())
    return 0


if __name__ == "__main__":
    sys.exit(main())
