package com.marcosbarbero.deskspace.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Fixed dates and predictable ids, in one place.
 *
 * Nothing in the test suite reads the wall clock or accepts whatever id it happened to
 * get, so a failure means a rule broke rather than that the suite ran across midnight.
 */
public final class Fixtures {

	public static final LocalDate TODAY = LocalDate.of(2026, 3, 2);

	public static final UUID A01 = UUID.fromString("11111111-0000-0000-0000-000000000001");

	public static final UUID A02 = UUID.fromString("11111111-0000-0000-0000-000000000002");

	public static final UUID UNKNOWN = UUID.fromString("99999999-0000-0000-0000-000000000000");

	private Fixtures() {
	}

	public static Clock clockAt(LocalDate date) {
		return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
	}

	public static Supplier<UUID> countingIds() {
		AtomicInteger counter = new AtomicInteger();
		return () -> UUID.fromString("22222222-0000-0000-0000-%012d".formatted(counter.incrementAndGet()));
	}

}
