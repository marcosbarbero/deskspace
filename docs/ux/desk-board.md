# Feature UX: the desk board

The one screen in [PRD 0001](../prd/0001-desk-booking.md). Implements
[the booking journey](../journeys/booking-a-desk.md).

## States

Every state below is reachable in the real system, so every state below has to be
designed. The ones that get skipped are always the bottom three, and those are
the ones a user hits on their worst day.

| state | what is on screen | why |
|---|---|---|
| **loading** | "Loading desks", announced to screen readers | the list is the whole screen; a blank frame reads as broken |
| **loaded, some free** | every desk, label and zone, a book button on the free ones | zone is visible before choosing, per step 3 of the journey |
| **loaded, none free** | the full list, every desk marked taken | showing an empty list would suggest the date is wrong rather than full |
| **conflict on booking** | the API's problem title, in place, list refreshed | step 5: she must be able to pick another without losing the date |
| **request failed** | the API's problem title | never a generic message; the API promised a title, so use it |
| **taken desk** | marked, no button | a disabled button invites a click that cannot work |
| **a zone with nothing in it** | the filter, and an empty list | the filter must survive every state, or the only way back is a reload |

## Rules

- **The failure message comes from the API.** The front end does not invent
  wording for a state the backend has already named. If a message reads badly,
  that is a defect in the API's problem title and it is fixed there, once, for
  every client.
- **A conflict does not clear the screen.** It is an in-place message plus a
  refreshed list.
- **Booked desks stay visible.** The list is a map of the room, not a menu.
- **The zone filter asks the API, it does not hide rows.** The server is the only
  thing that knows the whole room, and a client-side filter goes quietly wrong
  the first time the list is paged or capped. "Every zone" is an absent
  parameter, not a fourth zone.

## Accessibility

- Loading is `role="status"`, errors are `role="alert"`, so both are announced.
- Each book button names its desk: "Book A-01", not "Book". A screen reader user
  arrives at the button without the row context a sighted user has.
- The board has an accessible name that includes the date, so the answer to "what
  am I looking at" does not depend on having read the heading.

## Deliberately not designed

No optimistic update on booking. It would show a desk as booked before the server
agrees, and the one case that matters is precisely the one where the server
disagrees. The round trip is fast and honest.
