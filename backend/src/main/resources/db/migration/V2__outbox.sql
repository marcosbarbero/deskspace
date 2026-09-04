-- Events, made durable by the transaction that caused them.
--
-- Without this, a booking commits and the event is published afterwards, outside
-- any transaction. A crash in between leaves a booking nobody told the read
-- model about, and nothing ever retries.
create table outbox (
    id           uuid        primary key,
    type         varchar(200) not null,
    payload      jsonb       not null,
    occurred_at  timestamptz not null,
    published_at timestamptz
);

-- The relay only ever asks for unpublished rows, oldest first. A partial index
-- keeps that query on the small end of the table rather than on all of history.
create index outbox_unpublished on outbox (occurred_at) where published_at is null;
