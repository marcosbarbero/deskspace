package com.marcosbarbero.deskspace.availability.adapter.out.catalog;

import java.util.List;

import com.marcosbarbero.deskspace.availability.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.availability.domain.DeskSummary;
import com.marcosbarbero.deskspace.desk.application.DeskCatalog;

import org.springframework.stereotype.Component;

/**
 * The only class in availability that knows the desk slice exists, and the only place a
 * {@code Desk} becomes a {@code DeskSummary}. Both facts are asserted by
 * ArchitectureRulesTest rather than remembered.
 */
@Component
public class CatalogDeskSummaries implements DeskDirectory {

	private final DeskCatalog catalog;

	public CatalogDeskSummaries(DeskCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public List<DeskSummary> all() {
		return this.catalog.all()
			.stream()
			.map((desk) -> new DeskSummary(desk.id(), desk.label(), desk.zone().wireValue()))
			.toList();
	}

}
