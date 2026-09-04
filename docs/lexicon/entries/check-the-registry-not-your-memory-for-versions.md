---
key: check-the-registry-not-your-memory-for-versions
tags: [build, dependencies, agents]
---
# Pick dependency versions from the registry, never from memory

This repository was started on Spring Boot 3.4.1, chosen because it was a version
known to exist rather than because it was current. The current release was 4.1.1.
The upgrade turned out to be a one-line change with all 37 tests green, including
Pact verification, so the cost was not the migration. The cost was shipping
something visibly two majors behind and calling it a reference implementation.

This is a specific failure mode of working with an assistant: a model's sense of
"the latest version" is frozen at its training cutoff and it will state one
confidently, because a plausible version number and a correct one look identical.

**Do:** before pinning anything, ask the registry.

```bash
curl -s https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml \
  | grep -oE '<release>[^<]*</release>'
npm view <package> version
```

**And:** treat a version an assistant produced from memory as an unverified claim
in the same category as an API that might not exist.
