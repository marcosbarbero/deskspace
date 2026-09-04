package com.marcosbarbero.deskspace.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling exists only where there is an outbox to drain.
 *
 * Enabling it globally would start a scheduler in the default profile, which has nothing
 * for it to do and one more thread for a reader to wonder about.
 */
@Configuration
@Profile("postgres")
@EnableScheduling
public class OutboxScheduling {

}
