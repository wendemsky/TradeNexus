package com.fidelity.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fidelity.integration.ClientDao;
import com.fidelity.integration.DatabaseException;
//Importing models
import com.fidelity.models.Client;
import com.fidelity.models.ClientIdentification;
import com.fidelity.models.ClientPreferences;

class ClientServiceTest {
	@Mock ClientDao mockDao;
	@InjectMocks ClientService service;
	
	private List<Client> clientList;
	private List<ClientPreferences> clientPreferencesList;

	@BeforeEach
	void setUp() throws Exception {
		
		clientList = new ArrayList<Client>(List.of(
				new Client("rishiyanth@gmail.com","1425922638", "Password11", "Rishiyanth", "08/09/2002", "India", 
						new ArrayList<>(List.of(new ClientIdentification("Aadhar","123456789102"))), true),
				new Client("sowmya@gmail.com","1654658069", "Password12", "Sowmya", "02/01/2002", "India", 
						new ArrayList<>(List.of(new ClientIdentification("Aadhar","135395241534"))), true),
				new Client("john.doe@gmail.com","739982664", "Password123", "John Doe", "08/11/2003", "USA", 
						new ArrayList<>(List.of(new ClientIdentification("SSN","1234567890"))), false)
				));
		
		clientPreferencesList = new ArrayList<ClientPreferences>(
				List.of(
						new ClientPreferences("1425922638", "Education", "MIG", "Short", "Tier1", 5, true),
						new ClientPreferences("1654658069",  "Major Expense", "LIG", "Medium", "Tier2", 2, false),
						new ClientPreferences("739982665", "Retirement", "MIG", "Long", "Tier3", 3, true)
				)
			);
		//Initializing the client service with 2 clients and their preferences
		
		MockitoAnnotations.openMocks(this);
		service = new ClientService(mockDao);
		service.saveNewClientDetails(clientList.get(0));
		service.saveNewClientDetails(clientList.get(1));

	}

	@AfterEach
	void tearDown() throws Exception {
		service = null;
		clientList = null;
		clientPreferencesList = null;
	}

	@Test
	void shouldCreateObject() {
		assertNotNull(service);
	}
	
	/*Testing related to Client - Email Validation, Login and Register*/
	
	//Email Validation
	@Test
	void shouldHandleNullEmailValidation() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			Client client = service.verifyClientEmail(null);
		});
		assertEquals("Client Email cannot be null",e.getMessage());
	}
	
	@Test
	void testNonExistentEmailValidation() {
		Client client = service.verifyClientEmail("sample@gmail.com");
		assertNull(client,"Should return null for non existent email");
	}
	
	@Test
	void testExistentEmailValidation() {
		Client client = service.verifyClientEmail("sowmya@gmail.com");
		assertEquals(client,clientList.get(1),"Should return client details for existent email");
	}
	
	
	//Registering a new client
	@Test
	void shouldHandleRegisterationOfClientWithNullEmail() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			Client client = service.registerNewClient(null,"Password12", "John Doe", "08/11/2003", 
								"India", new ArrayList<>(List.of(new ClientIdentification("Aadhar","123467541244"))));
		});
		assertEquals("Client Email cannot be null",e.getMessage());
	}
	
	@Test
	void shouldHandleRegisterationOfExistingClient() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			Client client = service.registerNewClient("rishiyanth@gmail.com", "Password11", "Rishiyanth", "08/09/2002", 
								"India", new ArrayList<>(List.of(new ClientIdentification("Aadhar","123456789102"))));
		});
		assertEquals("Client with given email is already registered",e.getMessage());
	}
	
	@Test
	void shouldHandleRegisterationOfClientWithNullDetails() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			Client client = service.registerNewClient("john.doe@gmail.com","Password12", null, "08/11/2003", 
								"India", new ArrayList<>(List.of(new ClientIdentification("Aadhar","123467541244"))));
		});
		assertEquals("Client Details cannot be null",e.getMessage());
	}
	
	@Test
	void shouldHandleRegisterationOfClientWithIDDetailsOfExistingClient() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			Client client = service.registerNewClient("john.doe@gmail.com","Password12", "John Doe", "08/11/2003", 
								"India", new ArrayList<>(List.of(new ClientIdentification("Aadhar","135395241534"))));
		});
		assertEquals("Client with given Identification Details is already registered with another email",e.getMessage());
	}
	
	@Test
	void testSuccesfulRegistrationOfClientWithValidDetails() {
		Client newClient = service.registerNewClient("john.doe@gmail.com","Password123", "John Doe", "08/11/2003", 
				"USA", new ArrayList<>(List.of(new ClientIdentification("SSN","1234567890"))));
		assertEquals(newClient,clientList.get(2),"Should successfully register new client");
	}
	
	//Logging in an existing client
	@Test
	void shouldHandleLoginOfClientWithNullEmail() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			Client client = service.loginExistingClient(null,"Password12");
		});
		assertEquals("Client Email cannot be null",e.getMessage());
	}
	
	@Test
	void shouldHandleLoginOfNonExistentClient() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			Client client = service.loginExistingClient("sample_user@gmail.com","Password12");
		});
		assertEquals("Client with given email is not registered",e.getMessage());
	}
	
	@Test
	void shouldHandleLoginOfClientWithNullPassword() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			Client client = service.loginExistingClient("sowmya@gmail.com",null);
		});
		assertEquals("Client Password cannot be null",e.getMessage());
	}
	
	@Test
	void shouldHandleLoginOfExistentClientWithInvalidCredentials() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> {
			Client client = service.loginExistingClient("sowmya@gmail.com","Marsh12346");
		});
		assertEquals("Password does not match given Client's credentials",e.getMessage());
	}
	
	@Test
	void testSuccesfulLoginOfExistingValidClient() {
		Client existingClient = service.loginExistingClient("sowmya@gmail.com","Password12");
		assertEquals(existingClient,clientList.get(1),"Should successfully login existing client");	
	}
	
	/*Testing related to Adding and Updating of Client Preferences*/
	
	//Adding Client Preferences
	@Test
	void shouldAddClientPreference() {
		service.addClientPreferences(clientPreferencesList.get(2));
	}
	
	@Test
	void shouldHandleNullObjectForAddClientPreference() {
		assertThrows(NullPointerException.class, () -> {
			service.addClientPreferences(null);
		});
	}
	
	//Getting Client Preferences
	@Test
	void shouldGetClientPreference() {
		String id = "1425922638";
		ClientPreferences expected = clientPreferencesList.get(0);
		Mockito.when(mockDao.getClientPreferences(id))
			.thenReturn(expected);
		ClientPreferences clientPreference =  service.getClientPreference(id);
		
		assertEquals(clientPreference.equals(expected), true);
	}
	
	@Test
	void shouldHandleNullObjectForGetClientPreference() {
		assertThrows(NullPointerException.class, () -> {
			service.getClientPreference(null);
		});
	}
	
	//Updating Client Preferences
	@Test
	void shouldUpdateClientPreference() {
		ClientPreferences clientPreference = clientPreferencesList.get(0);
		assertEquals(clientPreference.getIncomeCategory(), "MIG");
		clientPreference.setIncomeCategory("HIG");
		service.updateClientPreferences(clientPreference);
		assertEquals(clientPreference.getIncomeCategory(), "HIG");
	}
	
	@Test
	void shouldNotUpdateForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			service.updateClientPreferences(null);
		});
	}
	
	@Test
	void testUpdationOfNonExistentClientPreferencesThrowsDatabaseException() {
		Mockito.doThrow(new DatabaseException()).when(mockDao).updateClientPreferences(clientPreferencesList.get(2));
		assertThrows(DatabaseException.class, () -> 
			service.updateClientPreferences(clientPreferencesList.get(2))
		);
	}
}
