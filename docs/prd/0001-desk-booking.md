# PRD 0001. Booking a desk

- Status: Shipping
- Owner: workspace operations
- Last updated: 2026-02-14

## The problem

A shared workspace has more people than desks on some days and more desks than
people on most. Today the allocation is a spreadsheet that one person maintains
and nobody trusts, so people arrive and find their desk occupied, and the
operations team spends the first hour of every morning resolving it in person.

## Who it is for

| | |
|---|---|
| **A member** | wants to know, before travelling in, that they have somewhere to sit |
| **Operations** | wants the morning back, and wants to know which zones are actually used |

Operations is a real user with real needs and **is out of scope for this PRD**.
Utilisation reporting is a separate problem and putting it here would make this
one unshippable.

## What success looks like

A member can see, for a given day, which desks are free, and take one, and give
it back. Nothing else.

- a member can determine availability for a date without asking anyone
- a desk cannot be held by two people for the same date
- releasing a desk makes it immediately available to somebody else

## Explicitly not in this version

Each of these was asked for and deferred, with the reason, so the deferral is a
decision rather than an omission:

| | why not now |
|---|---|
| recurring bookings | changes the data model from a booking to a rule that generates bookings; large, and worth doing once availability is proven |
| zone preferences and matching | needs usage data that does not exist yet, because the spreadsheet is not trustworthy enough to mine |
| check-in confirmation | solves no-shows, which is a real problem and a different one; a booking nobody uses is still better than a spreadsheet |
| authentication | the workspace has SSO already; wiring it is deployment work, not product work, and it would dominate this PRD |

## Constraints

- A member's identity, for now, is the email they type. It is a placeholder for
  SSO and everything downstream must treat it as an opaque identifier.
- Bookings are for a whole day. Half days were considered and are a pricing
  question, not a scheduling one.
- Today counts as bookable. Somebody standing in the lobby is the most common
  case, not an edge case.

## Journeys

- [Booking a desk for tomorrow](../journeys/booking-a-desk.md)

## Delivered by

- Issue #1 desk availability for a date
- Issue #2 booking a desk
- Issue #3 cancelling a booking
