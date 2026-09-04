[change] Record why a booking was cancelled

## Context

Operations cannot tell a room problem from a change of plan, because a cancelled
booking carries no reason. They currently ask in the corridor.

## Outcome

A cancellation may carry a reason, and operations can read it.

## Requirements

1. Cancelling a booking accepts an optional free-text reason of at most 200 characters.
2. A cancellation with no reason continues to succeed, unchanged.

## Test scenarios

Scenario: a member cancels and gives a reason
  Given a confirmed booking
  When it is cancelled with the reason "meeting moved"
  Then the cancelled booking carries that reason

Scenario: a member cancels without giving a reason
  Given a confirmed booking
  When it is cancelled with no reason
  Then the cancellation succeeds and the reason is absent

## Constraints

The existing 204 response shape must not change for clients that send no reason.

tier: 2
area: booking
