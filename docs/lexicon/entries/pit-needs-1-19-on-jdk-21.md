---
key: pit-needs-1-19-on-jdk-21
tags: [build, mutation, pit, jdk]
---
# PIT below 1.19 crashes its minion on JDK 21

Older PIT releases fail with an opaque minion crash on JDK 21 rather than a
version error, so it reads as a broken test rather than a version pin.

**Do:** keep `pitest.version` at 1.19.6 or later. It is a property in
`backend/pom.xml` with a comment saying why.
