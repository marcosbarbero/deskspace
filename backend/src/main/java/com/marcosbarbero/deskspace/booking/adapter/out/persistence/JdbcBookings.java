package com.marcosbarbero.deskspace.booking.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.booking.domain.BookingStatus;
import com.marcosbarbero.deskspace.booking.domain.DeskAlreadyBookedException;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * The same port, backed by Postgres.
 *
 * Nothing in the application layer changes or knows. That is the claim ports and adapters
 * makes, and this class is where it is either true or it is not.
 *
 * The one interesting line is the translation in {@link #save}. The database rejects a
 * duplicate with a driver exception, and letting that reach the web layer would turn a
 * 409 the contract promises into a 500. The service's own check is a fast path with a
 * good message; this is the one that holds under a race, and both have to produce the
 * same failure.
 */
@Component
@Profile("postgres")
public class JdbcBookings implements Bookings {

	private final JdbcClient jdbc;

	public JdbcBookings(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Booking save(Booking booking) {
		try {
			this.jdbc.sql("""
					insert into bookings (id, desk_id, booked_on, booked_by, status)
					values (:id, :deskId, :bookedOn, :bookedBy, :status)
					on conflict (id) do update set status = excluded.status
					""")
				.param("id", booking.id())
				.param("deskId", booking.deskId())
				.param("bookedOn", booking.date())
				.param("bookedBy", booking.bookedBy())
				.param("status", booking.status().name())
				.update();
		}
		catch (DuplicateKeyException ex) {
			throw new DeskAlreadyBookedException(booking.deskId(), booking.date());
		}
		return booking;
	}

	@Override
	public Optional<Booking> byId(UUID id) {
		return this.jdbc.sql("select * from bookings where id = :id")
			.param("id", id)
			.query(JdbcBookings::toBooking)
			.optional();
	}

	@Override
	public boolean isTaken(UUID deskId, LocalDate date) {
		return this.jdbc.sql("""
				select count(*) from bookings
				where desk_id = :deskId and booked_on = :date and status = 'CONFIRMED'
				""").param("deskId", deskId).param("date", date).query(Integer.class).single() > 0;
	}

	@Override
	public void deleteAll() {
		this.jdbc.sql("delete from bookings").update();
	}

	private static Booking toBooking(ResultSet row, int rowNumber) throws SQLException {
		return new Booking(row.getObject("id", UUID.class), row.getObject("desk_id", UUID.class),
				row.getObject("booked_on", LocalDate.class), row.getString("booked_by"),
				BookingStatus.valueOf(row.getString("status")));
	}

}
