# api

The contract. One file, and it is the source both sides are generated from.

| | |
|---|---|
| `openapi.yaml` | every endpoint, schema and error shape this system has |

Nothing else belongs here. No prose describing the API, because a second
description is a second source of truth that nobody checks.

**Changing it is the first step, never the last.** A change that touches a
request, a response or a status code is edited here, then both sides regenerate,
then the code catches up. `toolbox/verify` fails if the committed client types no
longer match this file.

See [ADR 0002](../docs/adr/0002-the-spec-is-the-contract.md) for why it is not
generated from the backend, and [ADR 0004](../docs/adr/0004-a-shared-types-package.md)
for why there is no shared types package.
