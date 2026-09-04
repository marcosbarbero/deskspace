#!/usr/bin/env python3
"""Validate an issue and print an implementable brief, or refuse it.

The first step of /work, before a token is spent on code. Most failed agent runs
are not bad implementations of a good ticket; they are competent implementations
of a ticket that never said enough, and the gap gets filled by guessing.

    issue_context.py 42            reads the issue with gh
    issue_context.py --file f.md   reads a local file, for offline work

Exit codes are the interface:
    0  implementable; the brief is on stdout
    1  not implementable; the reasons are on stderr. Do not repair the ticket
    2  tier 3; plan only, then stop and wait for a human
"""
from __future__ import annotations

import argparse
import re
import subprocess
import sys

SECTIONS = ("Context", "Outcome", "Requirements", "Test scenarios", "Constraints")
TIERS = {"1", "2", "3"}


def fetch(number: str) -> str:
    done = subprocess.run(["gh", "issue", "view", number, "--json", "title,body",
                           "--template", "{{.title}}\n\n{{.body}}"],
                          capture_output=True, text=True)
    if done.returncode != 0:
        print(f"could not read issue {number}: {done.stderr.strip()}", file=sys.stderr)
        raise SystemExit(1)
    return done.stdout


def section(text: str, name: str) -> str:
    m = re.search(rf"^#+\s*{re.escape(name)}\b.*?$(.*?)(?=^#+\s|\Z)", text, re.S | re.M | re.I)
    return m.group(1).strip() if m else ""


def numbered(block: str) -> list[str]:
    return [m.group(1).strip() for m in re.finditer(r"^\s*\d+[.)]\s+(.*)$", block, re.M)]


def scenarios(block: str) -> list[str]:
    return [m.group(1).strip() for m in re.finditer(r"^\s*(?:[-*]\s*)?(?:\*\*)?(Scenario:.*)$", block, re.M | re.I)]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("number", nargs="?")
    ap.add_argument("--file")
    args = ap.parse_args()

    if args.file:
        text = open(args.file).read()
    elif args.number:
        text = fetch(args.number.lstrip("gh#"))
    else:
        ap.error("give an issue number or --file")

    problems: list[str] = []
    found = {name: section(text, name) for name in SECTIONS}

    for name in ("Context", "Outcome", "Requirements", "Test scenarios"):
        if not found[name]:
            problems.append(f"no '{name}' section")

    reqs = numbered(found["Requirements"])
    scens = scenarios(found["Test scenarios"])

    if found["Requirements"] and not reqs:
        problems.append("Requirements are not numbered: a requirement without a number "
                        "cannot be pointed at by a scenario, a test, or a review")
    if found["Test scenarios"] and not scens:
        problems.append("no scenarios found: each must start with 'Scenario:'")
    if reqs and scens and len(scens) < len(reqs):
        problems.append(f"{len(reqs)} requirements but {len(scens)} scenarios: "
                        f"a requirement with no scenario will not get tested")

    tier = ""
    m = re.search(r"tier\s*[:\-]?\s*([123])", text, re.I)
    if m:
        tier = m.group(1)
    else:
        problems.append("no risk tier (tier: 1, 2 or 3)")

    if problems:
        print("REFUSED. This issue is not implementable as written:", file=sys.stderr)
        for p in problems:
            print(f"  - {p}", file=sys.stderr)
        print("\nThis is a defect in the ticket. Do not fill the gaps by guessing.", file=sys.stderr)
        return 1

    print(f"# Brief\n")
    for name in SECTIONS:
        if found[name]:
            print(f"## {name}\n\n{found[name]}\n")
    print(f"## Counts\n\n{len(reqs)} requirements, {len(scens)} scenarios, tier {tier}\n")
    print("Requirements are the specification. Scenarios are the verification.")
    print("Check they line up semantically before writing anything: a script cannot.")

    if tier == "3":
        print("\nTIER 3: produce the plan and stop. High-risk work is not auto-implemented.",
              file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
