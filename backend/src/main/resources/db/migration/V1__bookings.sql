-- Bookings, and the rule the service also checks.
--
-- BookDesk asks "is this desk taken" before writing, which is a check-then-act:
-- two requests arriving together both read free and both write. That check is a
-- fast path and a good error message. This index is the truth.
create table bookings (
    id         uuid         primary key,
    desk_id    uuid         not null,
    booked_on  date         not null,
    booked_by  varchar(254) not null,
    status     varchar(16)  not null
);

-- Partial, because a cancelled booking must not hold the desk. A plain unique
-- constraint on (desk_id, booked_on) would make cancelling and rebooking the
-- same desk on the same day impossible, which is a thing members do.
create unique index bookings_one_confirmed_per_desk_per_day
    on bookings (desk_id, booked_on)
    where status = 'CONFIRMED';

create index bookings_by_day on bookings (booked_on) where status = 'CONFIRMED';
