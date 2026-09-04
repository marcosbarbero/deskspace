---
key: checkstyle-needs-an-execution-id
tags: [build, checkstyle, maven]
---
# A bare checkstyle:check runs Sun's ruleset, not yours

Running `mvn checkstyle:check` without an execution id and `configLocation` uses
Checkstyle's bundled Sun ruleset instead of `src/checkstyle/quality-checkstyle.xml`,
and reports hundreds of violations nobody asked about.

**Do:** bind the plugin to an execution with an id and an explicit
`configLocation`, and invoke it through the lifecycle (`toolbox/verify`), not by goal.

**Cost of rediscovering it:** an afternoon spent believing the codebase is
catastrophically non-compliant.
