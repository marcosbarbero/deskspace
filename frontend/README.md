# frontend

React 19, TypeScript, Vite. The client is generated from
[`api/openapi.yaml`](../api/openapi.yaml); nothing that crosses the wire is
written by hand.

```bash
npm run gen        # regenerate src/api/schema.d.ts from the spec
npm run dev        # http://localhost:5173, proxying /api to :8080
npm run typecheck
npm run lint
npm test
npm run verify     # all of the above, in order
```

Usually you want `toolbox/verify` from the repository root instead.

## Layout

| | |
|---|---|
| `src/api/` | the generated schema and the one typed client |
| `src/features/booking/` | the desk board and its hook |

`src/api/schema.d.ts` is generated **and committed**, and `toolbox/verify`
regenerates it and fails on a diff. A spec change nobody regenerated is a spec
change this app has not seen.

## Two rules worth knowing before editing

**Failure wording belongs to the API.** The screen renders the problem title it
was sent and never invents one, so an error is worded once rather than three
times in three clients. See [`docs/ux/desk-board.md`](../docs/ux/desk-board.md).

**Every state is designed, including the bad ones.** Loading, empty, conflict and
request-failed are decided before the happy path is built, and each has a test.

TypeScript is pinned to 5.x because `openapi-typescript` does not work against
TypeScript 7. See the lexicon entry `openapi-typescript-breaks-on-ts7`.
