package com.marcosbarbero.deskspace.desk;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * The desks that physically exist. In-memory on purpose: this repository is a
 * demonstration of a delivery harness, and a database would add setup cost
 * without adding anything to what is being demonstrated.
 */
@Component
public class DeskCatalog {

	private final List<Desk> desks = List.of(
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000001"), "A-01", Zone.QUIET),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000002"), "A-02", Zone.QUIET),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000003"), "B-01", Zone.COLLABORATION),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000004"), "B-02", Zone.COLLABORATION),
			new Desk(UUID.fromString("11111111-0000-0000-0000-000000000005"), "L-01", Zone.LAB));

	public List<Desk> all() {
		return desks;
	}

	public Optional<Desk> byId(UUID id) {
		return desks.stream().filter(d -> d.id().equals(id)).findFirst();
	}

	public boolean exists(UUID id) {
		return byId(id).isPresent();
	}
}
