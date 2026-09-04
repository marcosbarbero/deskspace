---
key: spring-boot-4-split-autoconfiguration-into-modules
tags: [backend, spring, flyway, dependencies]
---
# Spring Boot 4 moved auto-configuration into per-technology modules

Adding `org.flywaydb:flyway-core` to a Spring Boot 4 application gives you the
library and no wiring. Migrations never run, nothing warns, and the first symptom
is `relation "bookings" does not exist` from a query, which reads as a broken
adapter rather than a missing dependency.

In Boot 3 the auto-configuration lived in the single
`spring-boot-autoconfigure` jar and `flyway-core` was enough. In Boot 4 it lives
in `spring-boot-flyway`, pulled in by `spring-boot-starter-flyway`.

**Do:** depend on the starter, not on the library.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
```

**How to check whether a technology has been split out:**

```bash
curl -s https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom \
  | grep -oE '<artifactId>spring-boot-(starter-)?flyway[^<]*'
```

**Related:** with `spring-boot-starter-jdbc` on the classpath and no datasource
configured, `DataSourceAutoConfiguration` fails every context that starts without
the database profile. `application.properties` excludes it and the postgres
profile sets `spring.autoconfigure.exclude=` to put it back.
