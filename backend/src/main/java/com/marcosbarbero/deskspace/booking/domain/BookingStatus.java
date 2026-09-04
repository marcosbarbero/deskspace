package com.marcosbarbero.deskspace.booking.domain;

public enum BookingStatus {

	CONFIRMED, CANCELLED;

	public String wireValue() {
		return name().toLowerCase();
	}

}
