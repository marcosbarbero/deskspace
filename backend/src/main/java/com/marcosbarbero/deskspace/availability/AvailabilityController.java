package com.marcosbarbero.deskspace.availability;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.marcosbarbero.deskspace.api.DesksApi;
import com.marcosbarbero.deskspace.api.model.ApiDesk;
import com.marcosbarbero.deskspace.api.model.ApiZone;
import com.marcosbarbero.deskspace.booking.Booking;
import com.marcosbarbero.deskspace.booking.BookingRepository;
import com.marcosbarbero.deskspace.desk.Desk;
import com.marcosbarbero.deskspace.desk.DeskCatalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Availability is the one place allowed to know about both desks and bookings. Keeping it
 * in its own slice is what lets desk stay ignorant of booking, which is the dependency
 * direction ArchitectureRulesTest enforces.
 */
@RestController
public class AvailabilityController implements DesksApi {

	private final DeskCatalog desks;

	private final BookingRepository bookings;

	public AvailabilityController(DeskCatalog desks, BookingRepository bookings) {
		this.desks = desks;
		this.bookings = bookings;
	}

	@Override
	public ResponseEntity<List<ApiDesk>> listDesks(LocalDate date) {
		Set<UUID> taken = bookings.confirmedOn(date).stream().map(Booking::deskId).collect(Collectors.toSet());
		return ResponseEntity.ok(desks.all().stream().map(desk -> toApi(desk, !taken.contains(desk.id()))).toList());
	}

	private ApiDesk toApi(Desk desk, boolean available) {
		return new ApiDesk(desk.id(), desk.label(), ApiZone.fromValue(desk.zone().name().toLowerCase()), available);
	}

}
