package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ClientDaoImplTest {
	
	static PoolableDataSource dataSource;
	ClientDao dao;
	TransactionManager transactionManager;

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
		dao = new ClientDaoImpl(dataSource);
		transactionManager = new TransactionManager(dataSource);
		transactionManager.startTransaction();
	}

	@AfterEach
	void tearDown() throws Exception {
		transactionManager.rollbackTransaction();
	}

	@Test
	void testForDAOToHaveBeenIntialized() {
		assertNotNull(dao);
	}
	
	/*TESTS FOR CLIENT EMAIL VERIFICATION*/
	//Existing email to be found
	@Test
	void testForClientWithExistingEmailToBeFound() {
		String existingEmail = "sowmya@gmail.com";
		Boolean clientExists = dao.verifyClientEmail(existingEmail);
		assertTrue(clientExists, "Client must be found");
	}
	//Non existing email to not be found
	@Test
	void testForClientWithNonExistingEmailToNotBeFound() {
		String nonExistingEmail = "client_nonexist@gmail.com";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.verifyClientEmail(nonExistingEmail);
		});
		assertEquals(e.getMessage(),"Client with given email doesnt exist");
	}
	//Invalid email to throw exception

	@Test
	@Disabled
	void testForClientWithInvalidEmailToThrowException() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.verifyClientEmail(null);
		});
		assertEquals(e.getMessage(),"Client with given email couldnt be retrieved");
	}

}
