package com.marcosbarbero.deskspace.availability.adapter.in.web;

import java.time.Instant;
import java.util.List;

import com.marcosbarbero.deskspace.availability.adapter.out.persistence.InMemoryOccupiedDesks;
import com.marcosbarbero.deskspace.availability.application.AvailabilityProjection;
import com.marcosbarbero.deskspace.availability.application.DesksOnDate;
import com.marcosbarbero.deskspace.availability.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.availability.domain.DeskSummary;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;
import com.marcosbarbero.deskspace.shared.adapter.in.web.ApiZoneConverter;
import com.marcosbarbero.deskspace.shared.adapter.in.web.ValidationProblemAdvice;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvailabilityApiTest {

	private static final List<DeskSummary> ROOM = List.of(new DeskSummary(Fixtures.A01, "A-01", "quiet"),
			new DeskSummary(Fixtures.A02, "A-02", "collaboration"));

	private InMemoryOccupiedDesks occupied;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		this.occupied = new InMemoryOccupiedDesks();
		DeskDirectory desks = () -> ROOM;
		this.mvc = MockMvcBuilders.standaloneSetup(new AvailabilityController(new DesksOnDate(desks, this.occupied)))
			.setControllerAdvice(new ValidationProblemAdvice())
			.setConversionService(conversionService())
			.build();
	}

	private static FormattingConversionService conversionService() {
		FormattingConversionService service = new DefaultFormattingConversionService();
		service.addConverter(new ApiZoneConverter());
		return service;
	}

	@Test
	void every_desk_is_free_when_nothing_is_booked() throws Exception {
		this.mvc.perform(get("/api/desks").param("date", Fixtures.TODAY.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].label").value("A-01"))
			.andExpect(jsonPath("$[0].zone").value("quiet"))
			.andExpect(jsonPath("$[0].available").value(true));
	}

	@Test
	void filters_by_zone() throws Exception {
		this.mvc.perform(get("/api/desks").param("date", Fixtures.TODAY.toString()).param("zone", "quiet"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].label").value("A-01"));
	}

	@Test
	void no_zone_returns_every_desk() throws Exception {
		this.mvc.perform(get("/api/desks").param("date", Fixtures.TODAY.toString()))
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void an_unknown_zone_is_refused_with_a_problem() throws Exception {
		this.mvc.perform(get("/api/desks").param("date", Fixtures.TODAY.toString()).param("zone", "library"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid request"))
			.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void a_booked_desk_is_not_available_on_that_date() throws Exception {
		new AvailabilityProjection(this.occupied)
			.on(new DeskBooked(Fixtures.UNKNOWN, Fixtures.A01, Fixtures.TODAY, Instant.EPOCH));

		this.mvc.perform(get("/api/desks").param("date", Fixtures.TODAY.toString()))
			.andExpect(jsonPath("$[0].available").value(false))
			.andExpect(jsonPath("$[1].available").value(true));
	}

}
