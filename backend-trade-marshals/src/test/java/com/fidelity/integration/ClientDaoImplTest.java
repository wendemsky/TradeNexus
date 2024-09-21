package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.ClientPreferences;

class ClientDaoImplTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	static PoolableDataSource dataSource;
	ClientDao dao;
	TransactionManager transactionManager;
	Connection connection = null;
	
	ClientPreferences client1654658069 = new ClientPreferences("1654658069", "Education", "HIG", "Short", "Tier4", 2, false);
	ClientPreferences client1654658070 = new ClientPreferences("1654658070", "Retirement", "LIG", "Short", "Tier4", 2, false);
	ClientPreferences client1654658000 = new ClientPreferences("1654658000", "Retirement", "LIG", "Short", "Tier4", 2, false);

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
		
		connection = dataSource.getConnection();
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
	
	@Test
	void testForGetClientPreferences() {
		String id = "1654658069";
		ClientPreferences expectedClientPreference = dao.getClientPreferences(id);
		assertTrue(expectedClientPreference.equals(client1654658069));
	}
	
	@Test
	void testForGetClientPreferencesForInvalidClientId() {
		String id = "1654658000";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientPreferences(id);
		});
		assertEquals(e.getMessage(), "Client ID does not exist");
	}
	
	@Test
	void testForAddingClientPreferences() throws SQLException {
		int oldCount = DbTestUtils.countRowsInTable(connection, "client_preferences");
		dao.addClientPreferences(client1654658070);
		int newCount = DbTestUtils.countRowsInTable(connection, "client_preferences");
		assertEquals(oldCount+1, newCount);
	}
	
	@Test
	void testForCheckingCorrectAddOfClientPreferences() throws SQLException {
		String id = "1654658070";
		String whereCondition = "client_id = "+ id;
		dao.addClientPreferences(client1654658070);
		int newCount = DbTestUtils.countRowsInTableWhere(connection, "client_preferences", whereCondition);
		assertEquals(1, newCount);
	}
	
	@Test
	void testForAddClientPreferencesMethodWhenClientDoesntExist() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.addClientPreferences(client1654658000);
		});
		assertEquals(e.getMessage(), "Cannot insert for Client that doenst exists");
	}
	
	@Test
	void testForAddClientPreferencesMethodForExistingClientPreferences() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.addClientPreferences(client1654658069);
		});
		assertEquals(e.getMessage(), "Cannot insert for Client ID that exists");
	}
	
	@Test
	void testForUpdatingClientPreferencesForValidClientId() throws SQLException {
		int oldCount = DbTestUtils.countRowsInTable(connection, "client_preferences");
		dao.updateClientPreferences(client1654658069);
		int newCount = DbTestUtils.countRowsInTable(connection, "client_preferences");
		assertEquals(oldCount, newCount);
	}

	@Test
	void testForUpdatingClientPreferencesFoInvalidClientId() throws SQLException {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.updateClientPreferences(client1654658000);
		});
		assertEquals(e.getMessage(), "Client ID does not exist");
	}
}
