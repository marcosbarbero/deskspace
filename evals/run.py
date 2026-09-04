#!/usr/bin/env python3
"""Run the harness evals: does each gate say yes and no where it should.

A gate nobody has watched reject something is a gate you are trusting on faith.
Every case below is an input, the verdict required, and the reason. Roughly half
assert a refusal, because that is the half that stops working silently.

    evals/run.py [--verbose] [--only <substring>]
"""
from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CASES = Path(__file__).parent / "cases"

GREEN, RED, DIM, RST = "\033[32m", "\033[31m", "\033[2m", "\033[0m"


@dataclass
class Case:
    gate: str
    name: str
    argv: list[str]
    expect_exit: int
    expect_in: str = ""
    stdin: str = ""
    setup: list[list[str]] = field(default_factory=list)
    teardown: list[list[str]] = field(default_factory=list)
    why: str = ""


def hook_payload(path: str) -> str:
    return json.dumps({"tool_input": {"file_path": path}})


def cases() -> list[Case]:
    ic = ["toolbox/issue_context.py", "--file"]
    hook = ["python3", ".claude/hooks/require_test_first.py"]
    return [
        # --- the ticket gate: refusing beats guessing -------------------------
        Case("issue-context", "accepts a complete ticket",
             ic + [str(CASES / "tickets" / "complete.md")], 0, "2 requirements, 2 scenarios",
             why="a well formed ticket must not be refused, or the gate gets removed"),
        Case("issue-context", "refuses fewer scenarios than requirements",
             ic + [str(CASES / "tickets" / "missing-scenario.md")], 1, "will not get tested",
             why="a requirement with no scenario is the most common way work ships untested"),
        Case("issue-context", "refuses unnumbered requirements",
             ic + [str(CASES / "tickets" / "unnumbered.md")], 1, "not numbered",
             why="an unnumbered requirement cannot be pointed at by a review"),
        Case("issue-context", "refuses a ticket with no tier",
             ic + [str(CASES / "tickets" / "no-tier.md")], 1, "no risk tier",
             why="without a tier, high-risk work is auto-implemented by default"),
        Case("issue-context", "stops at planning for tier 3",
             ic + [str(CASES / "tickets" / "tier-three.md")], 2, "TIER 3",
             why="irreversible work is planned by an agent and implemented by a human"),

        # --- test-first, per side of the wire ---------------------------------
        # Each case runs the hook against a scratch repository, so the verdict
        # depends on the case rather than on what happens to be uncommitted here.
        Case("test-first", "blocks backend production code with no backend test",
             ["evals/hook_offline.py", "backend/src/main/java/x/Booking.java"], 2, "test-first, backend",
             why="the gate's whole purpose; if this passes, nothing is enforced"),
        Case("test-first", "blocks frontend source with no frontend test",
             ["evals/hook_offline.py", "frontend/src/features/booking/DeskBoard.tsx"], 2, "test-first, frontend",
             why="a second deployable needs its own answer, not the backend's"),
        Case("test-first", "allows editing a test",
             ["evals/hook_offline.py", "backend/src/test/java/x/BookingTest.java"], 0,
             why="a gate that blocks writing the test it demands is unusable"),
        Case("test-first", "allows editing the spec",
             ["evals/hook_offline.py", "api/openapi.yaml"], 0,
             why="a wire change starts at the spec, before any test exists"),
        Case("test-first", "allows backend code once a backend test is on the branch",
             ["evals/hook_offline.py", "backend/src/main/java/x/Booking.java", "--with", "backend-test"], 0,
             why="the gate must open for the case it is asking for"),
        Case("test-first", "a backend test does not unlock the frontend",
             ["evals/hook_offline.py", "frontend/src/features/booking/DeskBoard.tsx",
              "--with", "backend-test"], 2, "test-first, frontend",
             why="the rule most harnesses get wrong the moment a second deployable exists"),
        Case("test-first", "a frontend test does not unlock the backend",
             ["evals/hook_offline.py", "backend/src/main/java/x/Booking.java",
              "--with", "frontend-test"], 2, "test-first, backend",
             why="the same rule, in the direction people forget to check"),

        # --- registries: drift must fail, not warn ----------------------------
        Case("tool-registry", "passes when every tool is registered",
             ["toolbox/tool_mapping.py", "check"], 0, "tool registry ok",
             why="the clean case, so a failure means drift rather than a broken checker"),
        Case("tool-registry", "fails on an unregistered tool",
             ["toolbox/tool_mapping.py", "check"], 1, "undiscoverable",
             setup=[["cp", "toolbox/lexicon.py", "toolbox/eval_scratch.py"]],
             teardown=[["rm", "-f", "toolbox/eval_scratch.py"]],
             why="an unregistered tool gets rebuilt by the next person who cannot find it"),
        Case("arch-map", "passes when the map matches the tree",
             ["toolbox/arch_map.py", "check"], 0, "architecture map ok",
             why="the clean case"),
        Case("arch-map", "fails when a package no slice claims appears",
             ["toolbox/arch_map.py", "check"], 1, "no slice claims it",
             setup=[["mkdir", "-p", "backend/src/main/java/com/marcosbarbero/deskspace/evalscratch"]],
             teardown=[["rm", "-rf", "backend/src/main/java/com/marcosbarbero/deskspace/evalscratch"]],
             why="a map that disagrees with the tree sends the next agent confidently wrong"),
        Case("lexicon", "passes when entries are well formed",
             ["toolbox/lexicon.py", "check"], 0, "lexicon ok",
             why="the clean case"),
        # --- the pull request gate --------------------------------------------
        Case("pr", "refuses a body that omits a scenario",
             ["python3", "evals/pr_offline.py", "cases/prs/omits-a-scenario.md"], 1,
             "scenario not in the coverage table",
             why="nobody cross-references four Gherkin blocks against a table at 6pm"),
        Case("pr", "refuses a body with no evidence the gates ran",
             ["python3", "evals/pr_offline.py", "cases/prs/no-evidence.md"], 1,
             "no evidence that the gates ran",
             why="a green claim with nothing behind it is the claim most often wrong"),
        Case("pr", "refuses a tier 3 issue outright",
             ["python3", "evals/pr_offline.py", "cases/prs/complete.md", "tier-three"], 1,
             "tier 3",
             why="irreversible work is opened by a human, not by a tool"),
        Case("pr", "accepts a complete body",
             ["python3", "evals/pr_offline.py", "cases/prs/complete.md"], 0, "covers every scenario",
             why="the clean case, or the gate gets removed for being unusable"),

        Case("lexicon", "finds a seeded lesson by keyword",
             ["toolbox/lexicon.py", "search", "checkstyle"], 0, "execution-id",
             why="a lexicon nobody can search is a directory of files"),
    ]


def run(case: Case, verbose: bool) -> tuple[bool, str]:
    for cmd in case.setup:
        subprocess.run(cmd, cwd=ROOT, capture_output=True)
    try:
        done = subprocess.run(case.argv, cwd=ROOT, input=case.stdin, text=True,
                              capture_output=True, timeout=120,
                              env={**os.environ, "HARNESS_SKIP_TEST_FIRST": "0"})
    finally:
        for cmd in case.teardown:
            subprocess.run(cmd, cwd=ROOT, capture_output=True)

    output = (done.stdout or "") + (done.stderr or "")
    if done.returncode != case.expect_exit:
        return False, f"exit {done.returncode}, expected {case.expect_exit}"
    if case.expect_in and case.expect_in not in output:
        return False, f"output does not contain {case.expect_in!r}"
    return True, output if verbose else ""


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--verbose", action="store_true")
    ap.add_argument("--only")
    args = ap.parse_args()

    selected = [c for c in cases()
                if not args.only or args.only in c.gate or args.only in c.name]
    failures = []
    gate = ""
    for case in selected:
        if case.gate != gate:
            gate = case.gate
            print(f"\n  {gate}")
        ok, detail = run(case, args.verbose)
        mark = f"{GREEN}ok{RST}  " if ok else f"{RED}FAIL{RST}"
        print(f"    {mark} {case.name}")
        if not ok:
            failures.append((case, detail))
            print(f"         {RED}{detail}{RST}")
            print(f"         {DIM}why it matters: {case.why}{RST}")
        elif args.verbose and detail:
            print("\n".join(f"         {DIM}{l}{RST}" for l in detail.splitlines()[:8]))

    refusals = sum(1 for c in selected if c.expect_exit != 0)
    print(f"\n  {len(selected)} cases, {refusals} of them asserting a refusal, "
          f"{len(failures)} failed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
