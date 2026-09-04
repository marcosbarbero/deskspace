package com.marcosbarbero.deskspace.booking.adapter.out.catalog;

import java.util.UUID;

import com.marcosbarbero.deskspace.booking.application.port.out.DeskDirectory;
import com.marcosbarbero.deskspace.desk.application.DeskCatalog;

import org.springframework.stereotype.Component;

/**
 * The only class in the booking slice that knows the desk slice exists.
 *
 * This is the whole point of the arrangement. The dependency is real and it is confined
 * to one file in the adapter layer, so it is visible in a diff and an architecture rule
 * can assert that it lives nowhere else. Replacing desks with a call to another service
 * changes this class and nothing above it.
 */
@Component
public class CatalogDeskExistence implements DeskDirectory {

	private final DeskCatalog catalog;

	public CatalogDeskExistence(DeskCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public boolean exists(UUID deskId) {
		return this.catalog.exists(deskId);
	}

}
