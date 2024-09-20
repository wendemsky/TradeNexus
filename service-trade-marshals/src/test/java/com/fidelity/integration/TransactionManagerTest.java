package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
