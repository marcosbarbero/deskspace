package com.marcosbarbero.deskspace.booking.adapter.out.catalog;

import com.marcosbarbero.deskspace.desk.adapter.out.persistence.InMemoryDesks;
import com.marcosbarbero.deskspace.desk.application.DeskCatalog;
import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The one class in booking that reaches into another slice. Small enough to be boring,
 * and tested anyway: an adapter that answered "yes" to everything would let a booking be
 * made against a desk that does not exist, and no test above it would fail.
 */
class CatalogDeskExistenceTest {

	private final CatalogDeskExistence existence = new CatalogDeskExistence(new DeskCatalog(new InMemoryDesks()));

	@Test
	void a_desk_in_the_catalogue_exists() {
		assertThat(this.existence.exists(Fixtures.A01)).isTrue();
	}

	@Test
	void a_desk_not_in_the_catalogue_does_not() {
		assertThat(this.existence.exists(Fixtures.UNKNOWN)).isFalse();
	}

}
