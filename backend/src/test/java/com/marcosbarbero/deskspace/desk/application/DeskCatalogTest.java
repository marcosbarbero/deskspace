package com.marcosbarbero.deskspace.desk.application;

import com.marcosbarbero.deskspace.desk.adapter.out.persistence.InMemoryDesks;
import com.marcosbarbero.deskspace.desk.domain.Desk;
import com.marcosbarbero.deskspace.desk.domain.Zone;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeskCatalogTest {

	private final DeskCatalog catalog = new DeskCatalog(new InMemoryDesks());

	@Test
	void lists_every_desk_in_the_room() {
		assertThat(this.catalog.all()).hasSize(5)
			.extracting(Desk::label)
			.containsExactly("A-01", "A-02", "B-01", "B-02", "L-01");
	}

	@Test
	void every_desk_belongs_to_a_zone() {
		assertThat(this.catalog.all()).extracting(Desk::zone)
			.containsExactly(Zone.QUIET, Zone.QUIET, Zone.COLLABORATION, Zone.COLLABORATION, Zone.LAB);
	}

	@Test
	void finds_a_desk_by_id() {
		assertThat(this.catalog.byId(Fixtures.A01)).isPresent();
		assertThat(this.catalog.exists(Fixtures.A01)).isTrue();
	}

	@Test
	void reports_an_unknown_id_as_absent() {
		assertThat(this.catalog.byId(Fixtures.UNKNOWN)).isEmpty();
		assertThat(this.catalog.exists(Fixtures.UNKNOWN)).isFalse();
	}

	@Test
	void a_zone_knows_its_wire_value() {
		assertThat(Zone.COLLABORATION.wireValue()).isEqualTo("collaboration");
	}

}
