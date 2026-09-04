---
key: vite-config-needs-vitest-defineconfig
tags: [frontend, vitest, vite]
---
# `test` in vite.config.ts requires vitest's defineConfig

Importing `defineConfig` from `vite` and adding a `test` block typechecks only
while the config file is outside the tsconfig include. The moment it is included,
`'test' does not exist in type 'UserConfigExport'`.

**Do:** `import { defineConfig } from 'vitest/config'`, and keep
`vite.config.ts` inside the tsconfig so it is actually checked.
