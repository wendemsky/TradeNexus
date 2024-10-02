package com.marshals.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.marshals.integration.ClientDaoImpl;
import com.marshals.integration.DatabaseException;
import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPreferences;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
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

}
