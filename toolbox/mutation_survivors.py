#!/usr/bin/env python3
"""Report surviving mutants, compactly.

PIT writes a multi-megabyte HTML report. An agent asked to "raise the mutation
score" will read it, spend tens of thousands of tokens, and summarise it badly.
The finding is a dozen lines. This prints those.

The answer was always small. Only the artifact was big.

Deterministic: same report in, same output out, no model involved.

    toolbox/mutation_survivors.py [--limit N] [--json] [report.xml]
"""
from __future__ import annotations

import argparse
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT = Path("backend/target/pit-reports/mutations.xml")


def survivors(report: Path):
    root = ET.parse(report).getroot()
    out, killed, total = [], 0, 0
    for m in root.iter("mutation"):
        total += 1
        if m.get("status") == "KILLED":
            killed += 1
            continue
        if m.get("status") in ("NO_COVERAGE", "SURVIVED"):
            out.append({
                "file": (m.findtext("sourceFile") or "?"),
                "line": int(m.findtext("lineNumber") or 0),
                "method": (m.findtext("mutatedMethod") or "?"),
                "mutator": (m.findtext("mutator") or "?").rsplit(".", 1)[-1],
                "status": m.get("status"),
            })
    return out, killed, total


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("report", nargs="?", default=str(DEFAULT))
    ap.add_argument("--limit", type=int, default=20)
    ap.add_argument("--json", action="store_true")
    args = ap.parse_args()

    report = Path(args.report)
    if not report.exists():
        print(f"no report at {report}. Run: cd backend && ./mvnw -Pmutation verify", file=sys.stderr)
        return 1

    found, killed, total = survivors(report)
    score = (killed / total * 100) if total else 0.0

    if args.json:
        print(json.dumps({"score": round(score, 1), "killed": killed, "total": total,
                          "survivors": found[: args.limit]}, indent=2))
        return 0

    if not found:
        print(f"no survivors. {killed}/{total} killed ({score:.0f}%)")
        return 0

    width = max(len(s["file"]) for s in found)
    for s in sorted(found, key=lambda s: (s["file"], s["line"]))[: args.limit]:
        print(f"  {s['file']:{width}}:{s['line']:<4} {s['method']:<22} {s['mutator']:<28} {s['status']}")
    extra = len(found) - args.limit
    if extra > 0:
        print(f"  ... and {extra} more")
    print(f"\n  {len(found)} survivors, {killed}/{total} killed ({score:.0f}%)")
    print("  A survivor means no test asserts the behaviour that mutant changed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
