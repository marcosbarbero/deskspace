package com.marcosbarbero.deskspace.shared.event;

/**
 * Outbound port: how a use case announces something happened.
 *
 * The application layer depends on this interface and never on Spring's publisher, so a
 * use case can be tested by handing it a list to append to, and moving to a broker is an
 * adapter change rather than a rewrite.
 */
public interface DomainEvents {

	void publish(DomainEvent event);

}
