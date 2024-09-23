package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fidelity.models.Holding;

class ClientActivityReportDaoImplTest {	
	static PoolableDataSource dataSource;
	ClientActivityReportDao dao;
	TransactionManager transactionManager;
	Connection connection = null;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		dataSource = new PoolableDataSource();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		dataSource.shutdown();
	}

	@BeforeEach
	void setUp() throws Exception {
		dao = new ClientActivityReportDaoImpl(dataSource);
		transactionManager = new TransactionManager(dataSource);
		transactionManager.startTransaction();
		
		connection = dataSource.getConnection();
	}

	@AfterEach
	void tearDown() throws Exception {
		transactionManager.rollbackTransaction();
	}

	@Test
	void testGetClientHoldings() {
		String clientId = "541107416";
		List<Holding> holdings = dao.getClientHoldings(clientId);
		assertTrue(holdings.size() > 4);
	}
	
	@Test
	void testGetClientHoldingsThrowsExceptionForNonExistingClientID() {
		String clientId = "nonExistingClientId";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientHoldings(clientId);
		});
		assertEquals("Client ID does not exist", e.getMessage());
	}

}
