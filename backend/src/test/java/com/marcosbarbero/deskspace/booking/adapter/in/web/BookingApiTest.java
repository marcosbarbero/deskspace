package com.marcosbarbero.deskspace.booking.adapter.in.web;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.marcosbarbero.deskspace.booking.adapter.out.persistence.InMemoryBookings;
import com.marcosbarbero.deskspace.booking.application.BookDesk;
import com.marcosbarbero.deskspace.booking.application.CancelBooking;
import com.marcosbarbero.deskspace.booking.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.booking.domain.Booking;
import com.marcosbarbero.deskspace.support.Fixtures;
import com.marcosbarbero.deskspace.support.RecordingDomainEvents;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The HTTP edge: status codes and problem bodies. Every error this slice can produce is
 * asserted, because api/openapi.yaml promises the shape and a browser parses it.
 */
class BookingApiTest {

	private BookDesk bookDesk;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		DeskDirectory desks = Set.of(Fixtures.A01, Fixtures.A02)::contains;
		InMemoryBookings bookings = new InMemoryBookings();
		this.bookDesk = new BookDesk(bookings, desks, new RecordingDomainEvents(), Fixtures.clockAt(Fixtures.TODAY),
				Fixtures.countingIds());
		CancelBooking cancel = new CancelBooking(bookings, new RecordingDomainEvents(),
				Fixtures.clockAt(Fixtures.TODAY));
		this.mvc = MockMvcBuilders.standaloneSetup(new BookingController(this.bookDesk, cancel))
			.setControllerAdvice(new BookingProblemAdvice())
			.build();
	}

	private String body(UUID deskId, LocalDate date, String who) {
		return """
				{"deskId":"%s","date":"%s","bookedBy":"%s"}""".formatted(deskId, date, who);
	}

	@Test
	void creating_a_booking_returns_201_and_the_booking() throws Exception {
		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(Fixtures.A01, Fixtures.TODAY, "ada@example.com")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("confirmed"))
			.andExpect(jsonPath("$.bookedBy").value("ada@example.com"))
			.andExpect(jsonPath("$.deskId").value(Fixtures.A01.toString()));
	}

	@Test
	void a_taken_desk_returns_409_with_a_problem() throws Exception {
		this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "grace@example.com");

		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(Fixtures.A01, Fixtures.TODAY, "ada@example.com")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Desk already booked"))
			.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void an_unknown_desk_returns_404_with_a_problem() throws Exception {
		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(Fixtures.UNKNOWN, Fixtures.TODAY, "ada@example.com")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Not found"));
	}

	@Test
	void a_date_in_the_past_returns_400_with_a_problem() throws Exception {
		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(Fixtures.A01, Fixtures.TODAY.minusDays(1), "ada@example.com")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid request"))
			.andExpect(jsonPath("$.detail").value(Matchers.containsString("in the past")));
	}

	@Test
	void cancelling_returns_204() throws Exception {
		Booking booking = this.bookDesk.book(Fixtures.A01, Fixtures.TODAY, "ada@example.com");

		this.mvc.perform(delete("/api/bookings/{id}", booking.id())).andExpect(status().isNoContent());
	}

	@Test
	void cancelling_an_unknown_booking_returns_404() throws Exception {
		this.mvc.perform(delete("/api/bookings/{id}", Fixtures.UNKNOWN)).andExpect(status().isNotFound());
	}

}
