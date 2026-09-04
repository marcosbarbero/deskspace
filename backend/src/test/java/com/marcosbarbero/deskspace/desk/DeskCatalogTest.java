package com.marcosbarbero.deskspace.desk;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeskCatalogTest {

	private final DeskCatalog catalog = new DeskCatalog();

	@Test
	void lists_every_desk_in_the_room() {
		assertThat(catalog.all()).hasSize(5)
			.extracting(Desk::label)
			.containsExactly("A-01", "A-02", "B-01", "B-02", "L-01");
	}

	@Test
	void every_desk_belongs_to_a_zone() {
		assertThat(catalog.all()).extracting(Desk::zone)
			.containsExactly(Zone.QUIET, Zone.QUIET, Zone.COLLABORATION, Zone.COLLABORATION, Zone.LAB);
	}

	@Test
	void finds_a_desk_by_id() {
		Desk first = catalog.all().get(0);

		assertThat(catalog.byId(first.id())).contains(first);
		assertThat(catalog.exists(first.id())).isTrue();
	}

	@Test
	void reports_an_unknown_id_as_absent() {
		UUID unknown = UUID.fromString("99999999-0000-0000-0000-000000000000");

		assertThat(catalog.byId(unknown)).isEmpty();
		assertThat(catalog.exists(unknown)).isFalse();
	}

}
