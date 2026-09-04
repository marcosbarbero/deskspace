package com.marcosbarbero.deskspace.desk.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.marcosbarbero.deskspace.desk.application.port.out.Desks;
import com.marcosbarbero.deskspace.desk.domain.Desk;
import com.marcosbarbero.deskspace.desk.domain.Zone;

import org.springframework.stereotype.Component;

/**
 * The room, as data. In memory on purpose: this repository demonstrates a delivery
 * harness and an architecture, and a database would add setup cost without adding
 * anything to either. See docs/adr/0005-in-memory-persistence.md.
 */
@Component
public class InMemoryDesks implements Desks {

	private static final List<Desk> DESKS = List.of(
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000001"), "A-01", Zone.QUIET),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000002"), "A-02", Zone.QUIET),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000003"), "B-01", Zone.COLLABORATION),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000004"), "B-02", Zone.COLLABORATION),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000005"), "L-01", Zone.LAB));

	@Override
	public List<Desk> all() {
		return DESKS;
	}

	@Override
	public Optional<Desk> byId(UUID id) {
		return DESKS.stream().filter((desk) -> desk.id().equals(id)).findFirst();
	}

}
