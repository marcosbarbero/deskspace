---
key: two-adapters-to-one-upstream-collide-on-bean-name
tags: [backend, spring, architecture]
---
# Two slices adapting the same upstream will collide on the default bean name

Booking and availability each need something from the desk slice, and each
declares its own outbound port. Naming both adapters `CatalogDeskDirectory`,
which reads well inside each slice, gives them the same default bean name and
Spring fails at context startup with `ConflictingBeanDefinitionException`.

It compiles. Only the tests that boot a context notice, which is why the failure
arrived in an integration test rather than anywhere near the classes involved.

**Do:** name an adapter after the question its port asks, not after the port.
`CatalogDeskExistence` (does this id name a desk) and `CatalogDeskSummaries`
(what is in the room) never collide, and each name says which of two different
needs it serves.

**And:** an adapter to another slice is not persistence. It belongs in
`adapter/out/<upstream>/`, so the directory says what the dependency is.
