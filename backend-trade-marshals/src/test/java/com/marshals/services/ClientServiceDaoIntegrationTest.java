package com.marshals.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTableWhere;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.marshals.integration.ClientDaoImpl;
import com.marshals.integration.DatabaseException;
import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class ClientServiceDaoIntegrationTest {

	@Autowired
	private ClientService service;
	
	@Autowired
	private JdbcTemplate testJdbcTemplate;
	
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
	
	/*CLIENT*/
	 
	/*TESTS FOR CLIENT EMAIL VERIFICATION*/
	//Existing email to be found
	@Test
	void testForClientWithExistingEmailToBeFound() {
		String existingEmail = "sowmya@gmail.com";
		Boolean clientExists = service.verifyClientEmail(existingEmail);
		assertTrue(clientExists, "Client must be found");
	}
	//Non existing email to not be found
	@Test
	void testForClientWithNonExistingEmailToNotBeFound() {
		String nonExistingEmail = "client_nonexist@gmail.com";
		Boolean clientExists = service.verifyClientEmail(nonExistingEmail);
		assertFalse(clientExists, "Client must not be found");
	}
	//Invalid email to throw exception
	@Test
	void testForClientWithInvalidEmailToThrowException() {
		String invalidEmail = "client_invalid";
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			service.verifyClientEmail(invalidEmail);
		});
		assertEquals(e.getMessage(),"Client Email Format is invalid");
	}
	
	/*TESTS FOR CLIENT LOGIN*/
	//Successful login of client
	@Test
	void testForSuccessfulRetrievalOfClientDetailsWithValidCredentialsFromService() {
		String validClientEmail = "sowmya@gmail.com";
		String validClientPassword = "Marsh2024";
		Client client = service.loginExistingClient(validClientEmail, validClientPassword);
		assertEquals(client1654658069, client, "Successful login must return Client details");
	}
	//Login of client with non existent email
	@Test
	void testForLoginOfNonExistentClientThrowsExceptionFromService() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			service.loginExistingClient(client1654658000.getEmail(), client1654658000.getPassword());
		}); 
		assertEquals(e.getMessage(), "Client with given email is not registered");
	}
	//Login of existing client with invalid credentials - password mismatch
	@Test
	void testForLoginOfExistingClientWithInvalidCredentailsThrowsExceptionFromService() {
		String existingClientEmail = "sowmya@gmail.com";
		String invalidClientPassword = "Password123";
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			service.loginExistingClient(existingClientEmail, invalidClientPassword);
		}); 
		assertEquals(e.getMessage(), "Password does not match logging in Client's credentials");
	}
	
//	/*TESTS FOR CLIENT REGISTRATION*/
	
	//Successful insetion must increase row count in client table
	@Test
	void testForSuccessfulAdditionOfNewClientDetailsInClientTableFromService() {
		int oldCount = countRowsInTable(testJdbcTemplate, "client");
		List<ClientIdentification> identificationList = new ArrayList<>(List.of(new ClientIdentification("SSN","1643846323")));
		Client client = service.registerNewClient("sam@gmail.com","Password1234", "Sam", "12/11/2000", "USA", identificationList);
		System.out.println(client);
		int newCount = countRowsInTable(testJdbcTemplate, "client");
		assertTrue(newCount == oldCount+1, "Client Table count must increase by one");
	}
	//Successful insertion must add correct data in client identification table
	@Test
	void testForSuccessfulAdditionOfNewClientDetailsInClientIdentificationTableFromService() {
		
		List<ClientIdentification> identificationList = new ArrayList<>(List.of(new ClientIdentification("SSN","1643846323")));
		Client newClient = service.registerNewClient("sam@gmail.com","Password1234", "Sam", "12/11/2000", "USA", identificationList);
		String newClientId = newClient.getClientId();
		String whereCondition = "client_id = "+ newClientId;
		int newCount = countRowsInTableWhere(testJdbcTemplate, "client_identification", whereCondition);
		System.out.println(newCount);
		assertTrue(newCount >= 1, "One or more client identification details added");
	}
	//Exisitng client registration throws error
	@Test
	void testForAdditionOfExistingClientDetailsShouldThrowExceptionFromService(){
		String existingClientId = "1654658069";
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			List<ClientIdentification> identificationList = new ArrayList<>(List.of(new ClientIdentification("Aadhar","123456789102")));
			service.registerNewClient("sowmya@gmail.com", "Marsh2024", "Sowmya", "11/12/2002", "India", identificationList);
		});
		assertEquals(e.getMessage(),"Client with given email is already registered");
	}
	
	@Test
	void testShouldHandleRegisterationOfClientWithIDDetailsOfExistingClientFromService() {
		List<ClientIdentification> identificationList = new ArrayList<>(List.of(new ClientIdentification("Aadhar","123456789102")));
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			service.registerNewClient("sam@gmail.com","Password1234", "Sam", "12/11/2000", "USA",
					identificationList);
		});
		assertEquals("Client with given Identification Details is already registered with another email",e.getMessage());
	}
	
	/*CLIENT PREFERENCES*/
	
	/*TESTS FOR GETTING CLIENT PREFERENCES*/
	@Test
	void testForGetClientPreferencesFromService() {
		String id = "1654658069";
		ClientPreferences expectedClientPreference = service.getClientPreferences(id);
		assertTrue(expectedClientPreference.equals(clientPref1654658069));
	}
	//Failure for non existent client preferences
	@Test
	void testForGetClientPreferencesForNonExistentClientFromService() {
		String id = "1654658000";
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.getClientPreferences(id);
		});
		assertEquals(e.getMessage(), "Client id does not exist in Database" );
	}
	
	/*TESTS FOR ADDING CLIENT PREFERENCES*/
	//Successful insertion must increase row count
	@Test
	void testForAddingClientPreferencesFromService(){
		//Must add new Client before adding new Client Preferences
		List<ClientIdentification> identificationList = new ArrayList<>(List.of(new ClientIdentification("SSN","1643846323")));
		Client newClient = service.registerNewClient("sam@gmail.com", "Password1234", "Sam", "12/11/2000", "USA", identificationList);
		ClientPreferences newClientPref = new ClientPreferences(newClient.getClientId(), "Retirement", "LIG", "Short", "Tier4", 2, false);
		int oldCount = countRowsInTable(testJdbcTemplate, "client_preferences");
		service.addClientPreferences(newClientPref);
		int newCount = countRowsInTable(testJdbcTemplate, "client_preferences");
		assertTrue(newCount == oldCount + 1, "Client Preferences count must increase by one");
	}
	
	//Successful insertion must add correct data
	@Test 
	void testForCheckingCorrectAddOfClientPreferencesFromService(){
		//Must add new Client before adding new Client Preferences
		List<ClientIdentification> identificationList = new ArrayList<>(List.of(new ClientIdentification("SSN","1643846323")));
		Client newClient = service.registerNewClient("sam@gmail.com", "Password1234", "Sam", "12/11/2000", "USA", identificationList);
		ClientPreferences newClientPref = new ClientPreferences(newClient.getClientId(), "Retirement", "LIG", "Short", "Tier4", 2, false);
		String newClientId = newClient.getClientId();
		String whereCondition = "client_id = "+ newClientId;
		service.addClientPreferences(newClientPref);
		int newCount = countRowsInTableWhere(testJdbcTemplate, "client_preferences", whereCondition);
		assertTrue(newCount == 1, "Exactly one client preferences details added");
	}
	//Failure - Insertion of client preferences for non existent client
	@Test
	void testForAddClientPreferencesMethodWhenClientDoesntExistFromService() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.addClientPreferences(clientPref1654658000);
		});
		assertEquals(e.getMessage(), "Error inserting client preferences - Should satisfy integrity constraints");
	}
	//Failure - Addition of client preferences to client with existing preferences - Can only update
	@Test
	void testForAddClientPreferencesMethodForExistingClientPreferencesFromService() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.addClientPreferences(clientPref1654658069);
		});
		assertEquals(e.getMessage(), "Error inserting client preferences - Should satisfy integrity constraints");
	}
	
	//Successful updation of client preferences
	@Test
	void testForUpdatingClientPreferencesForValidClientFromService() {
		String existingClientId = "541107416";
		String whereCondition = "length_of_investment = 'Long' and percentage_of_spend = 'Tier3' and is_advisor_accepted = 'false' and client_id = "+existingClientId;
		service.updateClientPreferences(clientPref541107416);
		int newCount = countRowsInTableWhere(testJdbcTemplate, "client_preferences", whereCondition);
		assertTrue(newCount==1,"Updated preferences must exist");
	}
	//Failure - Updation of preferences for non existent client
	@Test
	void testForUpdatingClientPreferencesForNonExistentClientFromService(){
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.updateClientPreferences(clientPref1654658000);
		});
		assertEquals(e.getMessage(), "Client doesn't exist");
	}

}
