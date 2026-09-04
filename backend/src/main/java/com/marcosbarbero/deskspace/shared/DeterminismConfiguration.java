package com.marcosbarbero.deskspace.shared;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Every source of non-determinism in this service is a bean.
 *
 * There are exactly two: what time it is, and what the next id will be. Because both are
 * injected, no test has to sleep, retry, or accept "whatever the id turned out to be",
 * and contract verification can pin a date rather than watch it drift into the past. See
 * docs/adr/0002 and the tests that use them.
 */
@Configuration
public class DeterminismConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	Supplier<UUID> idGenerator() {
		return UUID::randomUUID;
	}

}
