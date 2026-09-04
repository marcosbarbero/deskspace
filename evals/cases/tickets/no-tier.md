[change] Record why a booking was cancelled

## Context
Operations cannot tell a room problem from a change of plan.

## Outcome
A cancellation may carry a reason.

## Requirements
1. Cancelling accepts an optional reason.

## Test scenarios

Scenario: a member cancels and gives a reason
  Given a confirmed booking
  When it is cancelled with a reason
  Then the reason is recorded

area: booking
