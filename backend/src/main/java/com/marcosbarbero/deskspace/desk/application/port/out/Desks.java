package com.marcosbarbero.deskspace.desk.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.marcosbarbero.deskspace.desk.domain.Desk;

/** Outbound port: where desks come from. */
public interface Desks {

	List<Desk> all();

	Optional<Desk> byId(UUID id);

}
