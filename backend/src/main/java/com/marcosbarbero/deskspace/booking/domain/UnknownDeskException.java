package com.marcosbarbero.deskspace.booking.domain;

import java.util.UUID;

public class UnknownDeskException extends RuntimeException {

	public UnknownDeskException(UUID deskId) {
		super("No desk with id %s".formatted(deskId));
	}

}
