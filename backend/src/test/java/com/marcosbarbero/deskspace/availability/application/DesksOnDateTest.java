package com.marcosbarbero.deskspace.availability.application;

import java.time.Instant;
import java.util.List;

import com.marcosbarbero.deskspace.availability.adapter.out.persistence.InMemoryOccupiedDesks;
import com.marcosbarbero.deskspace.availability.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.availability.domain.DeskAvailability;
import com.marcosbarbero.deskspace.availability.domain.DeskSummary;
import com.marcosbarbero.deskspace.booking.domain.event.DeskBooked;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DesksOnDateTest {

	private static final List<DeskSummary> ROOM = List.of(new DeskSummary(Fixtures.A01, "A-01", "quiet"),
			new DeskSummary(Fixtures.A02, "A-02", "collaboration"));

	private InMemoryOccupiedDesks occupied;

	private DesksOnDate desksOnDate;

	@BeforeEach
	void setUp() {
		this.occupied = new InMemoryOccupiedDesks();
		DeskDirectory desks = () -> ROOM;
		this.desksOnDate = new DesksOnDate(desks, this.occupied);
	}

	private void bookA01() {
		new AvailabilityProjection(this.occupied)
			.on(new DeskBooked(Fixtures.UNKNOWN, Fixtures.A01, Fixtures.TODAY, Instant.EPOCH));
	}

	@Test
	void every_desk_is_available_when_nothing_is_occupied() {
		assertThat(this.desksOnDate.on(Fixtures.TODAY, null)).hasSize(2).allMatch(DeskAvailability::available);
	}

	@Test
	void reports_the_label_and_zone_it_was_given() {
		assertThat(this.desksOnDate.on(Fixtures.TODAY, null)).first().satisfies((availability) -> {
			assertThat(availability.label()).isEqualTo("A-01");
			assertThat(availability.zone()).isEqualTo("quiet");
		});
	}

	@Test
	void an_occupied_desk_is_not_available() {
		bookA01();

		List<DeskAvailability> result = this.desksOnDate.on(Fixtures.TODAY, null);

		assertThat(result).filteredOn((availability) -> availability.deskId().equals(Fixtures.A01))
			.singleElement()
			.matches((availability) -> !availability.available());
		assertThat(result).filteredOn((availability) -> availability.deskId().equals(Fixtures.A02))
			.singleElement()
			.matches(DeskAvailability::available);
	}

	@Test
	void filters_to_one_zone() {
		assertThat(this.desksOnDate.on(Fixtures.TODAY, "quiet")).singleElement()
			.satisfies((availability) -> assertThat(availability.deskId()).isEqualTo(Fixtures.A01));
	}

	@Test
	void no_zone_returns_every_desk() {
		assertThat(this.desksOnDate.on(Fixtures.TODAY, null)).hasSize(2);
	}

	@Test
	void availability_still_applies_within_a_zone() {
		bookA01();

		assertThat(this.desksOnDate.on(Fixtures.TODAY, "quiet")).singleElement()
			.matches((availability) -> !availability.available());
	}

	@Test
	void a_zone_with_no_desks_returns_nothing_rather_than_everything() {
		assertThat(this.desksOnDate.on(Fixtures.TODAY, "lab")).isEmpty();
	}

	@Test
	void an_occupied_desk_is_still_available_on_another_date() {
		bookA01();

		assertThat(this.desksOnDate.on(Fixtures.TODAY.plusDays(1), null)).allMatch(DeskAvailability::available);
	}

}
