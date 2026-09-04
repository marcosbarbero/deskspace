package com.marcosbarbero.deskspace.booking.adapter.in.web;

import java.util.UUID;

import com.marcosbarbero.deskspace.api.BookingsApi;
import com.marcosbarbero.deskspace.api.model.ApiBooking;
import com.marcosbarbero.deskspace.api.model.ApiBookingRequest;
import com.marcosbarbero.deskspace.booking.application.BookDesk;
import com.marcosbarbero.deskspace.booking.application.CancelBooking;
import com.marcosbarbero.deskspace.booking.domain.Booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound adapter. Translates HTTP into a use case call and a domain object back into the
 * wire type, and does nothing else. The {@code Api*} types stop here: an architecture
 * rule fails the build if one reaches the application layer.
 */
@RestController
public class BookingController implements BookingsApi {

	private final BookDesk bookDesk;

	private final CancelBooking cancelBooking;

	public BookingController(BookDesk bookDesk, CancelBooking cancelBooking) {
		this.bookDesk = bookDesk;
		this.cancelBooking = cancelBooking;
	}

	@Override
	public ResponseEntity<ApiBooking> createBooking(ApiBookingRequest request) {
		Booking booking = this.bookDesk.book(request.getDeskId(), request.getDate(), request.getBookedBy());
		return ResponseEntity.status(HttpStatus.CREATED).body(toApi(booking));
	}

	@Override
	public ResponseEntity<Void> cancelBooking(UUID bookingId) {
		this.cancelBooking.cancel(bookingId);
		return ResponseEntity.noContent().build();
	}

	private ApiBooking toApi(Booking booking) {
		return new ApiBooking(booking.id(), booking.deskId(), booking.date(), booking.bookedBy(),
				ApiBooking.StatusEnum.fromValue(booking.status().wireValue()));
	}

}
