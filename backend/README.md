# backend

Spring Boot 4.1, Java 21. The API described by [`api/openapi.yaml`](../api/openapi.yaml).

```bash
./mvnw test                  # unit tests, architecture rules, contract verification
./mvnw verify                # the above plus format, checkstyle, coverage
./mvnw -Pmutation verify     # plus the mutation score
./mvnw spring-boot:run       # http://localhost:8080
```

Usually you want `toolbox/verify` from the repository root instead, which runs
this, the front end and the registries.

## Layout

Packages are features, not layers. See
[ADR 0001](../docs/adr/0001-package-by-feature.md), and `toolbox/arch_map.py list`
for what each slice owns.

| slice | knows about |
|---|---|
| `desk` | nothing else. A desk exists whether or not anybody booked it |
| `booking` | `desk` |
| `availability` | `desk` and `booking`, and it is the only thing allowed to know both |
| `shared` | cross-cutting configuration only, no domain content |

`ArchitectureRulesTest` makes those edges a build failure rather than a paragraph.

## Two things that are not obvious

**Generated code lives in `target/`.** The controller interfaces and the `Api*`
wire types come from the spec on every build. They are never edited, never
committed, and never leave a controller.

**Nothing reads the wall clock.** `Clock` and the id supplier are beans in
`DeterminismConfiguration`, so every test pins them. The lexicon entry
`spring-cannot-choose-between-two-constructors` explains why that shape rather
than a second constructor.
