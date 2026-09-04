package com.marcosbarbero.deskspace.booking;

import com.marcosbarbero.deskspace.api.BookingsApi;
import com.marcosbarbero.deskspace.api.model.ApiBooking;
import com.marcosbarbero.deskspace.api.model.ApiBookingRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BookingController implements BookingsApi {

	private final BookingService service;

	public BookingController(BookingService service) {
		this.service = service;
	}

	@Override
	public ResponseEntity<ApiBooking> createBooking(ApiBookingRequest request) {
		Booking booking = service.book(request.getDeskId(), request.getDate(), request.getBookedBy());
		return ResponseEntity.status(HttpStatus.CREATED).body(toApi(booking));
	}

	@Override
	public ResponseEntity<Void> cancelBooking(UUID bookingId) {
		service.cancel(bookingId);
		return ResponseEntity.noContent().build();
	}

	private ApiBooking toApi(Booking booking) {
		return new ApiBooking(booking.id(), booking.deskId(), booking.date(), booking.bookedBy(),
				ApiBooking.StatusEnum.fromValue(booking.status().name().toLowerCase()));
	}

}
