---
key: openapi-typescript-breaks-on-ts7
tags: [frontend, codegen, typescript]
---
# openapi-typescript 7.13 breaks on TypeScript 7

TypeScript 7's ESM surface does not expose `ts.factory` the way the generator
expects, so `npm run gen` dies with
`TypeError: Cannot read properties of undefined (reading 'createKeywordTypeNode')`.
It looks like a broken spec. It is not.

**Do:** the front end pins `typescript@^5.9`. Check this before debugging the
OpenAPI file.
