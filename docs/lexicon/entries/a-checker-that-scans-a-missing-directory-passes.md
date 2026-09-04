---
key: a-checker-that-scans-a-missing-directory-passes
tags: [tooling, registries]
---
# A checker that scans a directory that no longer exists always passes

Moving the tools from `.claude/tools/` to `toolbox/` left `tool_mapping.py check`
globbing the old path. `Path.glob` on a missing directory yields nothing, so the
set of files on disk was empty, so nothing was ever unregistered, so the check
reported ok. Forever.

The eval caught it because one of its cases asserts the check **fails** when a
tool is unregistered. The clean-case test still passed and told us nothing.

**Do:** any check that enumerates a directory should assert the directory exists,
and its test suite must include a case where it fails. A gate with only a
happy-path test proves it runs, not that it gates.
