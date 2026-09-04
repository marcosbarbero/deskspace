---
key: testcontainers-2-renamed-every-module
tags: [backend, testing, testcontainers, dependencies]
---
# Testcontainers 2.x renamed every module

The coordinates everybody knows, `org.testcontainers:postgresql` and
`org.testcontainers:junit-jupiter`, stop at 1.21.4. Spring Boot 4.1 manages
Testcontainers 2.0.5, where the same modules are `testcontainers-postgresql` and
`testcontainers-junit-jupiter`.

The failure is unhelpful: "was not found in central", cached, and not reattempted
until the update interval elapses. It reads as a network problem.

**Do:** read the BOM rather than recalling the artifact id.

```bash
curl -s https://repo1.maven.org/maven2/org/testcontainers/testcontainers-bom/2.0.5/testcontainers-bom-2.0.5.pom \
  | grep artifactId
```

**And:** Spring Boot manages the `testcontainers.version` property but not the
individual modules, so each one still needs an explicit `<version>`.

**The general form:** the same as
[check-the-registry-not-your-memory-for-versions](check-the-registry-not-your-memory-for-versions.md),
one level down. A remembered *coordinate* is as unreliable as a remembered
version, and a major release is exactly when it goes stale.
