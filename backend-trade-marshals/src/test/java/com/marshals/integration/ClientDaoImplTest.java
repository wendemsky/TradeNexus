package com.marshals.integration;

import static com.marshals.integration.DbTestUtilsOld.countRowsInTable;
import static com.marshals.integration.DbTestUtilsOld.countRowsInTableWhere;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marshals.integration.ClientDao;
import com.marshals.integration.ClientDaoImpl;
import com.marshals.integration.DatabaseException;
import com.marshals.integration.TransactionManager;
import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;

class ClientDaoImplTest {
	
	static PoolableDataSource dataSource;
	ClientDao dao;
	TransactionManager transactionManager;
	Connection connection = null;
	
	//Few test client
	Client client1654658069 = new Client("sowmya@gmail.com","1654658069", "Marsh2024", "Sowmya", "11/12/2002", "India", 
			new ArrayList<>(List.of(new ClientIdentification("Aadhar","123456789102"))), true); //Existing client
	Client client1654658000 = new Client("nonexistent.client@gmail.com","1654658000", "Password1234", "Non Existent client", "12/10/1994", "USA", 
			new ArrayList<>(List.of(new ClientIdentification("SSN","1953826343"))), false); //Non existent client
	Client client1654658070 = new Client("sam@gmail.com","1654658070", "Password1234", "Sam", "12/11/2000", "USA", 
			new ArrayList<>(List.of(new ClientIdentification("SSN","1643846323"))), false); //New client to insert
	
	//Few test client preferences
	ClientPreferences clientPref1654658069 = new ClientPreferences("1654658069", "Education", "HIG", "Short", "Tier4", 2, false); //Existing client 
	ClientPreferences clientPref1654658070 = new ClientPreferences("1654658070", "Retirement", "LIG", "Short", "Tier4", 2, false); //New client to insert
	ClientPreferences clientPref541107416 =  new ClientPreferences("541107416", "Major Expense", "LIG", "Long", "Tier3", 1, false); //Existing client with updated preferences
	ClientPreferences clientPref1654658000 = new ClientPreferences("1654658000", "Retirement", "LIG", "Short", "Tier4", 2, false); //Non existent client

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
	
	/*CLIENT*/
	 
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
	void testForClientWithInvalidEmailToThrowException() {
		String invalidEmail = "client_invalid";
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			dao.verifyClientEmail(invalidEmail);
		});
		assertEquals(e.getMessage(),"Client Email Format is invalid");
	}
	
	/*TESTS FOR CLIENT LOGIN*/
	//Successful login of client
	@Test
	void testForSuccessfulRetrievalOfClientDetailsWithValidCredentials() {
		String validClientEmail = "sowmya@gmail.com";
		String validClientPassword = "Marsh2024";
		Client client = dao.getClientAtLogin(validClientEmail, validClientPassword);
		assertEquals(client1654658069, client, "Successful login must return Client details");
	}
	//Login of client with non existent email
	@Test
	void testForLoginOfNonExistentClientThrowsException() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientAtLogin(client1654658000.getEmail(), client1654658000.getPassword());
		}); 
		assertEquals(e.getMessage(), "Logging in Client doesnt exist");
	}
	//Login of existing client with invalid credentials - password mismatch
	@Test
	void testForLoginOfExistingClientWithInvalidCredentailsThrowsException() {
		String existingClientEmail = "sowmya@gmail.com";
		String invalidClientPassword = "Password123";
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			dao.getClientAtLogin(existingClientEmail, invalidClientPassword);
		}); 
		assertEquals(e.getMessage(), "Password does not match logging in Client's credentials");
	}
	
	/*TESTS FOR CLIENT REGISTRATION*/
	//Testing successful retrieval of all client identification details
	@Test
	void testForRetrievalOfAllClientIdentificationDetails() {
		List<ClientIdentification> identificationDetails = dao.getAllClientIdentificationDetails();
		assertTrue(identificationDetails.size()>=6,"Must retrieve atleast 6 client identification details");
	}
	//Successful insetion must increase row count in client table
	@Test
	void testForSuccessfulAdditionOfNewClientDetailsInClientTable() throws SQLException {
		int oldCount = countRowsInTable(connection, "client");
		dao.addNewClient(client1654658070, new ClientPortfolio(client1654658070.getClientId(),new BigDecimal("10000"),new ArrayList<>()));
		int newCount = countRowsInTable(connection, "client");
		assertTrue(newCount == oldCount+1, "Client Table count must increase by one");
	}
	//Successful insertion must add correct data in client identification table
	@Test
	void testForSuccessfulAdditionOfNewClientDetailsInClientIdentificationTable() throws SQLException {
		String newClientId = "1654658070";
		String whereCondition = "client_id = "+ newClientId;
		dao.addNewClient(client1654658070, new ClientPortfolio(client1654658070.getClientId(),new BigDecimal("10000"),new ArrayList<>()));
		int newCount = countRowsInTableWhere(connection, "client_identification", whereCondition);
		assertTrue(newCount >= 1, "One or more client identification details added");
	}
	//Successful insetion must increase row count
	@Test
	void testForAdditionOfExistingClientDetailsShouldThrowException() throws SQLException {
		String existingClientId = "1654658069";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.addNewClient(client1654658069, 
					new ClientPortfolio(existingClientId,new BigDecimal("10000"),new ArrayList<>()));
		});
		assertEquals(e.getMessage(),"Cannot insert client with ID "+existingClientId);
	}
	
	
	/*CLIENT PREFERENCES*/
	
	/*TESTS FOR GETTING CLIENT PREFERENCES*/
	//Successful retrieval
	@Test
	void testForGetClientPreferences() {
		String id = "1654658069";
		ClientPreferences expectedClientPreference = dao.getClientPreferences(id);
		assertTrue(expectedClientPreference.equals(clientPref1654658069));
	}
	//Failure for non existent client preferences
	@Test
	void testForGetClientPreferencesForNonExistentClient() {
		String id = "1654658000";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientPreferences(id);
		});
		assertEquals(e.getMessage(), "Client ID does not exist");
	}
	
	/*TESTS FOR ADDING CLIENT PREFERENCES*/
	//Successful insetion must increase row count
	@Test
	void testForAddingClientPreferences() throws SQLException {
		//Must add new Client before adding new Client Preferences
		dao.addNewClient(client1654658070, new ClientPortfolio(client1654658070.getClientId(),new BigDecimal("10000"),new ArrayList<>()));
		int oldCount = countRowsInTable(connection, "client_preferences");
		dao.addClientPreferences(clientPref1654658070);
		int newCount = countRowsInTable(connection, "client_preferences");
		assertEquals(oldCount+1, newCount);
	}
	//Successful insertion must add correct data
	@Test
	void testForCheckingCorrectAddOfClientPreferences() throws SQLException {
		String newClientId = "1654658070";
		//Must add new Client before adding new Client Preferences
		dao.addNewClient(client1654658070, new ClientPortfolio(newClientId,new BigDecimal("10000"),new ArrayList<>()));
		String whereCondition = "client_id = "+ newClientId;
		dao.addClientPreferences(clientPref1654658070);
		int newCount = countRowsInTableWhere(connection, "client_preferences", whereCondition);
		assertEquals(1, newCount);
	}
	//Failure - Insertion of client preferences for non existent client
	@Test
	void testForAddClientPreferencesMethodWhenClientDoesntExist() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.addClientPreferences(clientPref1654658000);
		});
		assertEquals(e.getMessage(), "Cannot insert preferences for Client that doenst exist");
	}
	//Failure - Addition of client preferences to client with existing preferences - Can only update
	@Test
	void testForAddClientPreferencesMethodForExistingClientPreferences() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.addClientPreferences(clientPref1654658069);
		});
		assertEquals(e.getMessage(), "Cannot insert preferences for Client with existing preferences");
	}
	
	/*TESTS FOR UPDATING CLIENT PREFERENCES*/
	//Successful updation of client preferences
	@Test
	void testForUpdatingClientPreferencesForValidClient() throws SQLException {
		String existingClientId = "541107416";
		String whereCondition = "length_of_investment = 'Long' and percentage_of_spend = 'Tier3' and is_advisor_accepted = 'false' and client_id = "+existingClientId;
		dao.updateClientPreferences(clientPref541107416);
		int newCount = countRowsInTableWhere(connection, "client_preferences",whereCondition);
		assertTrue(newCount==1,"Updated preferences must exist");
	}
	//Failure - Updation of preferences for non existent client
	@Test
	void testForUpdatingClientPreferencesForNonExistentClient() throws SQLException {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.updateClientPreferences(clientPref1654658000);
		});
		assertEquals(e.getMessage(), "Cannot update preferences for Client that doenst exist");
	}
}
