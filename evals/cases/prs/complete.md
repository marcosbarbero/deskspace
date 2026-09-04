Closes #1

## What changed

A cancellation may now carry a free-text reason of up to 200 characters, and
operations can read it. Cancellations without a reason behave exactly as before.

## Requirements covered

| # | requirement | scenario | test |
|---|---|---|---|
| 1 | optional reason, max 200 chars | Scenario: a member cancels and gives a reason | `BookingServiceTest.records_a_cancellation_reason` |
| 2 | no reason still succeeds | Scenario: a member cancels without giving a reason | `BookingApiTest.cancelling_without_a_reason_returns_204` |

## Evidence

```
toolbox/verify --mutation
green. 39 tests, 34/34 mutants killed, line coverage 88%
```

## Lessons

Nothing surprised me.
