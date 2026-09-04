package com.marcosbarbero.deskspace.availability.application.port.out;

import java.util.List;

import com.marcosbarbero.deskspace.availability.domain.DeskSummary;

/**
 * Outbound port: the desks to report on, in this slice's own terms.
 *
 * Availability declares its own rather than sharing booking's. They ask different
 * questions of the same slice, and one shared interface is how a port stops describing a
 * need and starts describing a table.
 */
public interface DeskDirectory {

	List<DeskSummary> all();

}
