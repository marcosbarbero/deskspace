package com.marcosbarbero.deskspace.booking;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import com.marcosbarbero.deskspace.shared.ProblemAdvice;

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
 * The HTTP edge: status codes and the problem body. Every error response this service can
 * produce is asserted here, because api/openapi.yaml promises the shape and a client
 * parses it.
 */
class BookingApiTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 3, 2);

	private static final UUID DESK = UUID.fromString("11111111-0000-0000-0000-000000000001");

	private static final UUID UNKNOWN = UUID.fromString("99999999-0000-0000-0000-000000000000");

	private MockMvc mvc;

	private BookingService service;

	@BeforeEach
	void setUp() {
		Clock fixed = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
		this.service = new BookingService(new BookingRepository(), new com.marcosbarbero.deskspace.desk.DeskCatalog(),
				fixed, UUID::randomUUID);
		this.mvc = MockMvcBuilders.standaloneSetup(new BookingController(this.service))
			.setControllerAdvice(new ProblemAdvice())
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
				.content(body(DESK, TODAY, "ada@example.com")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("confirmed"))
			.andExpect(jsonPath("$.bookedBy").value("ada@example.com"))
			.andExpect(jsonPath("$.deskId").value(DESK.toString()));
	}

	@Test
	void a_taken_desk_returns_409_with_a_problem() throws Exception {
		this.service.book(DESK, TODAY, "grace@example.com");

		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(DESK, TODAY, "ada@example.com")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Desk already booked"))
			.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void an_unknown_desk_returns_404_with_a_problem() throws Exception {
		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(UNKNOWN, TODAY, "ada@example.com")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.title").value("Not found"));
	}

	@Test
	void a_date_in_the_past_returns_400_with_a_problem() throws Exception {
		this.mvc
			.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
				.content(body(DESK, TODAY.minusDays(1), "ada@example.com")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid request"))
			.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("past")));
	}

	@Test
	void cancelling_returns_204() throws Exception {
		Booking booking = this.service.book(DESK, TODAY, "ada@example.com");

		this.mvc.perform(delete("/api/bookings/{id}", booking.id())).andExpect(status().isNoContent());
	}

	@Test
	void cancelling_an_unknown_booking_returns_404() throws Exception {
		this.mvc.perform(delete("/api/bookings/{id}", UNKNOWN)).andExpect(status().isNotFound());
	}

}
