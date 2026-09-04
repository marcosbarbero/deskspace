Closes #1

## What changed

A cancellation may now carry a reason.

## Requirements covered

| # | requirement | scenario | test |
|---|---|---|---|
| 1 | optional reason | Scenario: a member cancels and gives a reason | `BookingServiceTest.records_a_reason` |
| 2 | no reason still succeeds | Scenario: a member cancels without giving a reason | `BookingApiTest.cancel_without_reason` |

## Evidence

It all works, I ran the tests locally.
