package com.marcosbarbero.deskspace.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * A transaction manager for the profile that has nothing to coordinate.
 *
 * The use cases are transactional because with a database the booking and its outbox row
 * must commit together. Without a database there is no resource to enlist, and Spring
 * would fail at the first call with no manager at all.
 *
 * Saying "there is nothing to coordinate here" in fifteen lines is better than the
 * alternatives: making the use case ask which profile it is in, or making the default
 * clone need a database to run.
 */
@Configuration
@Profile("!postgres")
public class NoDatabaseTransactions {

	@Bean
	PlatformTransactionManager transactionManager() {
		return new PlatformTransactionManager() {

			@Override
			public TransactionStatus getTransaction(TransactionDefinition definition) {
				return new SimpleTransactionStatus();
			}

			@Override
			public void commit(TransactionStatus status) {
			}

			@Override
			public void rollback(TransactionStatus status) {
			}

		};
	}

}
