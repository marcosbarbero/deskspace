# 0008. Ports and adapters within each slice

- Status: Accepted
- Date: 2026-03-05
- Refines: [0001](0001-package-by-feature.md)

## Context

[0001](0001-package-by-feature.md) made packages features rather than layers, and
that held. What it did not say is what a feature looks like inside, and the
answer that emerged by default was a service, a repository and a controller in
one flat package, each importing the next.

That arrangement has a specific cost, and it showed up as soon as anything had to
be tested. The booking rules could not be exercised without deciding how bookings
are stored, because the class that held the rules named the class that held the
storage. Every test of a rule was therefore also a test of a map.

There is a second cost that matters more here than in most repositories. An agent
asked to change a booking rule has to read the storage class to know whether it is
allowed to, and every file it must read to be safe is context it must be given.

## Options considered

### A. Ports and adapters inside each slice (chosen)

`domain` holds types and rules and depends on nothing of ours. `application`
holds use cases and declares the interfaces it needs. `adapter/in` holds things
that call in, `adapter/out` holds things the application calls. Dependencies
point inward, always.

### B. Flat slice: service, repository, controller (rejected)

What was here before. Fewer files and fewer packages, and it makes the
interesting half of the code untestable without the uninteresting half. It also
makes "does this change cross a boundary" unanswerable, because there is no
boundary to cross.

### C. Ports and adapters at the application level, not per slice (rejected)

One `domain`, one `application`, one `adapter` for the whole service. It is the
usual first reading of hexagonal architecture and it undoes
[0001](0001-package-by-feature.md): a change to booking would touch three
top-level packages again, and the blast radius of a feature would be unbounded.

## Decision

Each slice is its own hexagon. Ports are declared by the side that needs them,
which is why `booking.application.port.out.DeskDirectory` has exactly one method:
booking needs to know an id names something real, and needs nothing else about
desks.

## Consequences

There are more files. That is the honest cost and it is not nothing.

What is bought: `BookDeskTest` sets up in four lines with no container, the
architecture rules can be stated as sentences about packages, and the one place
booking touches another slice is a single named class an architecture rule can
find.

The rules in `ArchitectureRulesTest` found four real violations the first time
they ran, including one design mistake nobody had noticed: availability's port
returned the desk slice's `Desk`, which quietly made a shared kernel out of two
slices that were supposed to be independent. That became
`availability.domain.DeskSummary`.
