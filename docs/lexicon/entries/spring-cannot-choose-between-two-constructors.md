---
key: spring-cannot-choose-between-two-constructors
tags: [backend, spring, testing]
---
# Two constructors on a bean means Spring picks neither

Adding a package-private constructor so a test can inject a fake left
`BookingService` with two constructors. Spring does not error clearly; it falls
back to a no-arg constructor that does not exist, and the context fails with
`No default constructor found`.

**Do:** one public constructor. If a test needs to inject something, make that
thing a bean too. Both sources of non-determinism here, the `Clock` and the id
supplier, live in `DeterminismConfiguration` for exactly this reason.
