# toolbox

Scripts anyone can run: a person, CI, or an agent. They take arguments, print to
stdout, and use their exit code as the answer.

```bash
toolbox/tool_mapping.py list      # what is in here, by key
toolbox/tool_mapping.py get <key> # detail for one
```

`mapping.json` is the registry and `tool_mapping.py check` fails the build when a
script here is not in it, because an unregistered tool is an undiscoverable tool
and gets rebuilt by the next person who cannot find it.

## Why these are not in `.claude/`

`.claude/hooks/` holds event handlers bound to an agent runtime: they read a JSON
payload on stdin and answer with an exit code at a moment somebody else chose.
Nothing here is like that. These are ordinary scripts, and `toolbox/verify` is
called by the git hook and by CI as much as by anyone else.

Keeping them apart means the harness is not a Claude feature. Swap the agent and
the toolbox is untouched.

## Why a folder rather than the repository root

A script at the root has nothing stopping it becoming eleven scripts at the root.
One directory with a registry and a check is the version that survives.
