package com.marshals.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marshals.integration.TransactionManager;

class TransactionManagerTest {
	static PoolableDataSource dataSource;
	TransactionManager transactionManager;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		dataSource = new PoolableDataSource();
	}

	@BeforeEach
	void setUp() throws Exception {
		transactionManager = new TransactionManager(dataSource);		
		transactionManager.startTransaction();
	}

	@AfterEach
	void tearDown() throws Exception {
		transactionManager.rollbackTransaction();
	}

	@AfterAll
	static void cleanup() {
		dataSource.shutdown();
	}
	
	@Test
	void testTransactionManagerCreated() {
		assertNotNull(transactionManager);
	}

}
