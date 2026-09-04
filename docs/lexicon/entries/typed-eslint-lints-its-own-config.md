---
key: typed-eslint-lints-its-own-config
tags: [frontend, eslint]
---
# Type-aware ESLint rules applied to eslint.config.js fail confusingly

Spreading `tseslint.configs.recommendedTypeChecked` at the top level applies
type-aware rules to `eslint.config.js` itself, which is not in any tsconfig
project, and the error blames a rule rather than the config shape.

**Do:** put type-checked config inside a block with `files: ['**/*.{ts,tsx}']`,
and add `disableTypeChecked` for `**/*.js`.
