[change] Record why a booking was cancelled

## Context
Operations cannot tell a room problem from a change of plan.

## Outcome
A cancellation may carry a reason.

## Requirements
1. Cancelling accepts an optional reason of at most 200 characters.
2. A cancellation with no reason continues to succeed.
3. Operations can filter cancellations by reason.

## Test scenarios

Scenario: a member cancels and gives a reason
  Given a confirmed booking
  When it is cancelled with the reason "meeting moved"
  Then the cancelled booking carries that reason

tier: 2
area: booking
