Closes #1

## What changed

A cancellation may now carry a reason.

## Requirements covered

| # | requirement | scenario | test |
|---|---|---|---|
| 1 | optional reason | Scenario: a member cancels and gives a reason | `BookingServiceTest.records_a_reason` |

## Evidence

```
./verify --mutation
green.
```
