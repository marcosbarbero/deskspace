package com.marcosbarbero.deskspace.shared.event.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class JdbcOutbox implements Outbox {

	private final JdbcClient jdbc;

	public JdbcOutbox(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/**
	 * The payload is cast in SQL rather than bound as a driver-specific type, so the
	 * Postgres driver stays a runtime dependency and never appears in code anybody
	 * compiles against.
	 */
	@Override
	public void record(StoredEvent event) {
		this.jdbc.sql("""
				insert into outbox (id, type, payload, occurred_at, published_at)
				values (:id, :type, cast(:payload as jsonb), :occurredAt, null)
				""")
			.param("id", event.id())
			.param("type", event.type())
			.param("payload", event.payload())
			.param("occurredAt", Timestamp.from(event.occurredAt()))
			.update();
	}

	@Override
	public List<StoredEvent> unpublished(int limit) {
		return this.jdbc.sql("""
				select id, type, payload, occurred_at from outbox
				where published_at is null
				order by occurred_at, id
				limit :limit
				""").param("limit", limit).query(JdbcOutbox::toStored).list();
	}

	@Override
	public void markPublished(UUID id) {
		this.jdbc.sql("update outbox set published_at = now() where id = :id and published_at is null")
			.param("id", id)
			.update();
	}

	private static StoredEvent toStored(ResultSet row, int rowNumber) throws SQLException {
		// The driver will not hand back an Instant from timestamptz, but it will
		// hand back an OffsetDateTime, which carries the same moment.
		return new StoredEvent(row.getObject("id", UUID.class), row.getString("type"), row.getString("payload"),
				row.getObject("occurred_at", OffsetDateTime.class).toInstant());
	}

}
