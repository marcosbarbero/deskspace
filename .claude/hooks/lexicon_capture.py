#!/usr/bin/env python3
"""Stop: if this session changed production code and recorded nothing, say so.

Honest about its limit. Recall can be fully automatic; capture cannot, because
"was that surprising?" is a judgement no script can make. A hook that demanded an
entry every session would fill the lexicon with noise, and a lexicon full of
noise is worse than an empty one.

So it does the one deterministic thing available: notices that production code
changed and no entry was added, and asks at the only moment the answer is still
fresh.

Non-blocking on purpose. A missing entry costs something real but small. Blocking
a finished session costs more, and gets the hook deleted.
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from hook_io import git, repo_root  # noqa: E402

PRODUCTION = ("backend/src/main/", "frontend/src/", "api/openapi.yaml", "toolbox/")

NUDGE = """\
This session changed production code and added no lexicon entry.

If anything here was non-obvious, a framework behaviour that surprised you, a
boundary that was not what it looked like, a build failure whose cause you had to
hunt for, then the next session pays for it again unless it is written down now:

  toolbox/lexicon.py add --key <slug> --title "<what to know>" --tags <area>

If nothing was surprising, that is a fine answer and needs no entry.
"""


def changed(root: Path) -> set[str]:
    out: set[str] = set()
    for args in (("diff", "--name-only"), ("diff", "--cached", "--name-only"),
                 ("ls-files", "--others", "--exclude-standard")):
        out |= {line for line in git(*args, cwd=root).splitlines() if line.strip()}
    return out


def main() -> int:
    root = repo_root()
    if root is None:
        return 0
    files = changed(root)
    touched_production = any(f.startswith(PRODUCTION) for f in files)
    added_entry = any(f.startswith("docs/lexicon/entries/") for f in files)
    if touched_production and not added_entry:
        print(NUDGE, file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
