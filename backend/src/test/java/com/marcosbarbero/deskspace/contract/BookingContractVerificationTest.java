package com.marcosbarbero.deskspace.contract;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.marcosbarbero.deskspace.booking.Booking;
import com.marcosbarbero.deskspace.booking.BookingRepository;
import com.marcosbarbero.deskspace.booking.BookingStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Verifies the pact the browser client published against this service.
 *
 * The OpenAPI spec says what this API can do. The pact says what one consumer
 * actually depends on, which is a much smaller set and the one that must keep
 * working. Removing a field nobody reads is safe; removing a field named in a
 * pact fails here, before it fails in someone's browser.
 */
@Provider("deskspace-backend")
@PactFolder("../pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BookingContractVerificationTest.FixedClock.class)
class BookingContractVerificationTest {

	/**
	 * The pact names a specific date. With a real clock that date moves into the
	 * past and the service starts refusing it, so the contract would rot on a
	 * calendar rather than on a change anybody made. See ADR 0002 on
	 * deterministic time.
	 */
	static final LocalDate CONTRACT_DATE = LocalDate.of(2026, 3, 2);

	private static final UUID DESK_A01 = UUID.fromString("11111111-0000-0000-0000-000000000001");

	@TestConfiguration
	static class FixedClock {

		// A different bean name, marked primary: replacing the definition outright
		// would need bean overriding enabled, which hides real duplicate-bean bugs.
		@Bean
		@Primary
		Clock contractClock() {
			return Clock.fixed(CONTRACT_DATE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
		}
	}

	@LocalServerPort
	private int port;

	@Autowired
	private BookingRepository bookings;

	@BeforeEach
	void target(PactVerificationContext context) {
		bookings.deleteAll();
		context.setTarget(new HttpTestTarget("localhost", this.port));
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verifiesTheConsumerContract(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@State("desk A-01 exists and is free on 2026-03-02")
	void deskIsFree() {
		// The catalogue is fixed and nothing is booked: deleteAll ran in @BeforeEach.
	}

	@State("desk A-01 is already booked on 2026-03-02")
	void deskIsTaken() {
		bookings.save(new Booking(UUID.fromString("22222222-0000-0000-0000-000000000001"), DESK_A01, CONTRACT_DATE,
				"grace@example.com", BookingStatus.CONFIRMED));
	}
}
