# Journey: booking a desk

**Ada, a member, on the train, the evening before.**

She is coming in tomorrow for the first time in three weeks and wants to know
there is somewhere to sit before she commits to the trip.

| # | She does | She needs to see | Goes wrong when |
|---|---|---|---|
| 1 | opens the board | tomorrow's date, already selected | it opens on today, and she books the wrong day without noticing |
| 2 | scans the list | which desks are free, and roughly where each one is | zones are codes rather than words, so "B-02" tells her nothing |
| 3 | picks one near the quiet zone | the zone next to each desk | zone is only visible after selecting, so choosing means guessing and undoing |
| 4 | books it | that it worked, without a page reload losing her place | the confirmation is a toast that vanishes before she looks up |
| 5 | is beaten to it by half a second | a clear, specific message that this desk went, and the rest of the list still usable | a generic failure that makes her reload and lose the date |
| 6 | picks another | the list already updated | she books a desk that was taken while she read the error |

## Where this journey actually fails

Step 5. Two people booking the last desk in the quiet zone at the same time is
not a rare case in a workspace where everyone books the evening before, and it is
the only step where the system has to say no to somebody who did nothing wrong.

That is why the API has a distinct `409` with its own problem title rather than a
generic error: the front end has to be able to say *this desk*, not *something
went wrong*, and it has to be able to recover in place.

## What she never does

She never cancels from this screen. Cancelling happens the next morning, from a
different context, usually because plans changed. Putting cancel on the board
because the API supports it would be building for the API rather than for Ada.
