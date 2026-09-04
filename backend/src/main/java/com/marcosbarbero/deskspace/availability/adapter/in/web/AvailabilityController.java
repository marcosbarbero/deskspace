package com.marcosbarbero.deskspace.availability.adapter.in.web;

import java.time.LocalDate;
import java.util.List;

import com.marcosbarbero.deskspace.api.DesksApi;
import com.marcosbarbero.deskspace.api.model.ApiDesk;
import com.marcosbarbero.deskspace.api.model.ApiZone;
import com.marcosbarbero.deskspace.availability.application.DesksOnDate;
import com.marcosbarbero.deskspace.availability.domain.DeskAvailability;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvailabilityController implements DesksApi {

	private final DesksOnDate desksOnDate;

	public AvailabilityController(DesksOnDate desksOnDate) {
		this.desksOnDate = desksOnDate;
	}

	@Override
	public ResponseEntity<List<ApiDesk>> listDesks(LocalDate date, ApiZone zone) {
		String wanted = (zone != null) ? zone.getValue() : null;
		return ResponseEntity
			.ok(this.desksOnDate.on(date, wanted).stream().map(AvailabilityController::toApi).toList());
	}

	private static ApiDesk toApi(DeskAvailability availability) {
		return new ApiDesk(availability.deskId(), availability.label(), ApiZone.fromValue(availability.zone()),
				availability.available());
	}

}
