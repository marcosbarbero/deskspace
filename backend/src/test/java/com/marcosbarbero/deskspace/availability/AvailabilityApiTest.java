package com.marcosbarbero.deskspace.availability;

import java.time.LocalDate;
import java.util.UUID;

import com.marcosbarbero.deskspace.booking.Booking;
import com.marcosbarbero.deskspace.booking.BookingRepository;
import com.marcosbarbero.deskspace.booking.BookingStatus;
import com.marcosbarbero.deskspace.desk.DeskCatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Availability is the only place that knows about desks and bookings at once, so it is
 * the only place where "free" can be wrong. Asserted here rather than inferred from the
 * two halves passing separately.
 */
class AvailabilityApiTest {

	private static final LocalDate DATE = LocalDate.of(2026, 3, 2);

	private static final UUID A01 = UUID.fromString("11111111-0000-0000-0000-000000000001");

	private BookingRepository bookings;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		this.bookings = new BookingRepository();
		this.mvc = MockMvcBuilders.standaloneSetup(new AvailabilityController(new DeskCatalog(), this.bookings))
			.build();
	}

	@Test
	void every_desk_is_free_when_nothing_is_booked() throws Exception {
		this.mvc.perform(get("/api/desks").param("date", DATE.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(5))
			.andExpect(jsonPath("$[0].label").value("A-01"))
			.andExpect(jsonPath("$[0].zone").value("quiet"))
			.andExpect(jsonPath("$[0].available").value(true))
			.andExpect(jsonPath("$[4].available").value(true));
	}

	@Test
	void a_booked_desk_is_not_available_on_that_date() throws Exception {
		this.bookings.save(new Booking(UUID.randomUUID(), A01, DATE, "grace@example.com", BookingStatus.CONFIRMED));

		this.mvc.perform(get("/api/desks").param("date", DATE.toString()))
			.andExpect(jsonPath("$[0].available").value(false))
			.andExpect(jsonPath("$[1].available").value(true));
	}

	@Test
	void a_booking_on_another_date_leaves_the_desk_available() throws Exception {
		this.bookings
			.save(new Booking(UUID.randomUUID(), A01, DATE.plusDays(1), "grace@example.com", BookingStatus.CONFIRMED));

		this.mvc.perform(get("/api/desks").param("date", DATE.toString()))
			.andExpect(jsonPath("$[0].available").value(true));
	}

	@Test
	void a_cancelled_booking_leaves_the_desk_available() throws Exception {
		this.bookings.save(new Booking(UUID.randomUUID(), A01, DATE, "grace@example.com", BookingStatus.CANCELLED));

		this.mvc.perform(get("/api/desks").param("date", DATE.toString()))
			.andExpect(jsonPath("$[0].available").value(true));
	}

}
