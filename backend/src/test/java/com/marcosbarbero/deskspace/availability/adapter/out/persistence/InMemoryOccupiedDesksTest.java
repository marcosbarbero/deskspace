package com.marcosbarbero.deskspace.availability.adapter.out.persistence;

import com.marcosbarbero.deskspace.support.Fixtures;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryOccupiedDesksTest {

	private final InMemoryOccupiedDesks occupied = new InMemoryOccupiedDesks();

	@Test
	void nothing_is_occupied_to_begin_with() {
		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
	}

	@Test
	void occupying_records_the_desk_for_that_date_only() {
		this.occupied.occupy(Fixtures.A01, Fixtures.TODAY);

		assertThat(this.occupied.on(Fixtures.TODAY)).containsExactly(Fixtures.A01);
		assertThat(this.occupied.on(Fixtures.TODAY.plusDays(1))).isEmpty();
	}

	@Test
	void releasing_removes_only_that_desk() {
		this.occupied.occupy(Fixtures.A01, Fixtures.TODAY);
		this.occupied.occupy(Fixtures.A02, Fixtures.TODAY);

		this.occupied.release(Fixtures.A01, Fixtures.TODAY);

		assertThat(this.occupied.on(Fixtures.TODAY)).containsExactly(Fixtures.A02);
	}

	@Test
	void releasing_a_desk_that_was_never_occupied_leaves_the_others_alone() {
		this.occupied.occupy(Fixtures.A02, Fixtures.TODAY);

		this.occupied.release(Fixtures.A01, Fixtures.TODAY);

		assertThat(this.occupied.on(Fixtures.TODAY)).containsExactly(Fixtures.A02);
	}

	@Test
	void releasing_on_a_date_with_nothing_on_it_is_harmless() {
		this.occupied.release(Fixtures.A01, Fixtures.TODAY);

		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
	}

	@Test
	void clearing_empties_every_date() {
		this.occupied.occupy(Fixtures.A01, Fixtures.TODAY);
		this.occupied.occupy(Fixtures.A02, Fixtures.TODAY.plusDays(1));

		this.occupied.clear();

		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
		assertThat(this.occupied.on(Fixtures.TODAY.plusDays(1))).isEmpty();
	}

	@Test
	void the_returned_set_is_a_copy() {
		this.occupied.occupy(Fixtures.A01, Fixtures.TODAY);

		var snapshot = this.occupied.on(Fixtures.TODAY);
		this.occupied.release(Fixtures.A01, Fixtures.TODAY);

		assertThat(snapshot).containsExactly(Fixtures.A01);
		assertThat(this.occupied.on(Fixtures.TODAY)).isEmpty();
	}

}
