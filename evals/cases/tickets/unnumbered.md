[change] Record why a booking was cancelled

## Context
Operations cannot tell a room problem from a change of plan.

## Outcome
A cancellation may carry a reason.

## Requirements
- Cancelling accepts an optional reason.
- A cancellation with no reason still succeeds.

## Test scenarios

Scenario: a member cancels and gives a reason
  Given a confirmed booking
  When it is cancelled with a reason
  Then the reason is recorded

Scenario: a member cancels without a reason
  Given a confirmed booking
  When it is cancelled
  Then it succeeds

tier: 2
area: booking
