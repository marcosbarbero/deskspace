package com.marcosbarbero.deskspace.desk.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.marcosbarbero.deskspace.desk.application.port.out.Desks;
import com.marcosbarbero.deskspace.desk.domain.Desk;

import org.springframework.stereotype.Service;

/**
 * The desks that exist, as a use case rather than as a repository call. Other slices
 * reach this through a port of their own, never by importing it.
 */
@Service
public class DeskCatalog {

	private final Desks desks;

	public DeskCatalog(Desks desks) {
		this.desks = desks;
	}

	public List<Desk> all() {
		return this.desks.all();
	}

	public Optional<Desk> byId(UUID id) {
		return this.desks.byId(id);
	}

	public boolean exists(UUID id) {
		return byId(id).isPresent();
	}

}
