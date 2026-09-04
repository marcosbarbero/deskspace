---
key: spring-boot-4-ships-jackson-3-under-a-new-package
tags: [backend, spring, jackson, dependencies]
---
# Spring Boot 4 auto-configures Jackson 3, whose package is `tools.jackson`

Jackson 3 moved from `com.fasterxml.jackson` to `tools.jackson`. Spring Boot 4
configures the Jackson 3 `ObjectMapper`.

The trap is that Jackson 2 is usually still on the classpath, dragged in by some
other library. So a class written from memory with
`import com.fasterxml.jackson.databind.ObjectMapper` **compiles**, and then no
bean of that type exists, and the context fails at startup with an
unsatisfied-dependency message that names a type you can see in your imports.

**Do:** `import tools.jackson.databind.ObjectMapper`. Check what is actually
there before assuming:

```bash
./mvnw -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
tr ':' '\n' < /tmp/cp.txt | grep jackson
```

**Two smaller consequences.** Jackson 3 handles `java.time` without
`JavaTimeModule`, and its write methods no longer throw a checked
`JsonProcessingException`, so `try`/`catch` blocks copied from Jackson 2 will not
compile.

**The general form:** a compiling import is not evidence that it is the type your
framework wired up. Two majors of the same library on one classpath is the case
where "it compiles" tells you nothing at all.
