package com.marcosbarbero.deskspace.desk.domain;

public enum Zone {

	QUIET, COLLABORATION, LAB;

	public String wireValue() {
		return name().toLowerCase();
	}

}
