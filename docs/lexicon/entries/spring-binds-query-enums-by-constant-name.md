---
key: spring-binds-query-enums-by-constant-name
tags: [backend, spring, openapi]
---
# Spring binds a query enum by constant name, not by the spec's value

Adding `zone` to `GET /api/desks` as an enum in `api/openapi.yaml` generates
`ApiZone` with constants `QUIET`, `COLLABORATION`, `LAB` and values `"quiet"`,
`"collaboration"`, `"lab"`. Spring's default enum binding calls
`Enum.valueOf(type, source)`, which is case sensitive and knows nothing about
the value.

So `?zone=quiet` returns 400 for a value the published contract says is valid,
and the failure looks like a validation bug rather than a binding one.

**Do:** register a `Converter<String, ApiZone>` calling the generated
`fromValue`. `ApiZoneConverter` does this. Unknown values throw there, Spring
reports a type mismatch, and `ValidationProblemAdvice` turns it into the problem
body the contract promises.

**Watch for:** a standalone `MockMvcBuilders` setup does not pick the converter
up from the context. Give it one with `.setConversionService(...)` or the test
will disagree with the running service.

**The general form:** generated code describes a wire format; how a framework
binds to it is a separate decision, and the default is usually the framework's
convention rather than your contract's.
