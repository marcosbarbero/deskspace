[change] Move member identity from a typed email to SSO

## Context

A booking is attributed to whatever email the member typed. Two people typing the
same address are the same member, and a typo is a member nobody can find. The
workspace already has SSO.

## Outcome

A booking is attributed to an authenticated subject, and existing bookings keep
resolving to the right person.

## Requirements

1. A booking records the authenticated subject rather than a typed address.
2. Existing bookings are migrated to subjects where an address matches exactly.
3. An existing booking whose address matches no subject is retained and flagged.

## Test scenarios

Scenario: a booking made after the change records the subject
  Given an authenticated member
  When they book a desk
  Then the booking records their subject

Scenario: an existing booking with a matching address is migrated
  Given a booking made with an address that matches a subject
  When the migration runs
  Then the booking records that subject

Scenario: an existing booking with no matching address is retained
  Given a booking made with an address that matches no subject
  When the migration runs
  Then the booking is retained and flagged for review

## Constraints

No booking may be deleted by the migration.

tier: 3
area: booking
