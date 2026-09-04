package com.marcosbarbero.deskspace.contract;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.marcosbarbero.deskspace.availability.application.port.out.OccupiedDesks;
import com.marcosbarbero.deskspace.booking.application.BookDesk;
import com.marcosbarbero.deskspace.booking.application.port.out.Bookings;
import com.marcosbarbero.deskspace.support.Fixtures;

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
 * The spec says what this API may do. The pact says what one consumer depends on, which
 * is smaller and stricter. Removing a field nobody reads is safe; changing a status named
 * in a pact fails here rather than in someone's browser.
 *
 * Provider states set up through the real use case, so the read model is populated by the
 * same event path production uses. A state that reached into the projection directly
 * would verify a system nobody runs.
 */
@Provider("deskspace-backend")
@PactFolder("../pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BookingContractVerificationTest.ContractClock.class)
class BookingContractVerificationTest {

	/**
	 * The pact names a date. With a real clock that date drifts into the past and the
	 * service starts refusing it, so the contract would rot on a calendar rather than on
	 * a change somebody made, and a red build nobody caused is the worst kind.
	 */
	static final LocalDate CONTRACT_DATE = LocalDate.of(2026, 3, 2);

	private static final UUID DESK_A01 = UUID.fromString("11111111-0000-0000-0000-000000000001");

	@TestConfiguration
	static class ContractClock {

		@Bean
		@Primary
		Clock contractClock() {
			return Fixtures.clockAt(CONTRACT_DATE);
		}

	}

	@LocalServerPort
	private int port;

	@Autowired
	private Bookings bookings;

	@Autowired
	private OccupiedDesks occupied;

	@Autowired
	private BookDesk bookDesk;

	@BeforeEach
	void target(PactVerificationContext context) {
		this.bookings.deleteAll();
		this.occupied.clear();
		context.setTarget(new HttpTestTarget("localhost", this.port));
	}

	@State("desk A-01 exists and is free on 2026-03-02")
	void deskIsFree() {
		// deleteAll and clear in @BeforeEach already leave it free.
	}

	@State("desk A-01 is already booked on 2026-03-02")
	void deskIsTaken() {
		this.bookDesk.book(DESK_A01, CONTRACT_DATE, "grace@example.com");
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verifiesTheConsumerContract(PactVerificationContext context) {
		context.verifyInteraction();
	}

}
