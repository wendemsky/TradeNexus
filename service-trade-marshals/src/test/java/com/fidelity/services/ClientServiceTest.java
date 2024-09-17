package com.fidelity.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Importing models
import com.fidelity.models.Client;
import com.fidelity.models.ClientIdentification;
import com.fidelity.models.ClientPreferences;

class ClientServiceTest {
	
	private ClientService service;
	
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
						new ClientPreferences("1425922638", "Education", "MIG", "Short", "Tier1", 5, "true"),
						new ClientPreferences("1654658069",  "Major Expense", "LIG", "Medium", "Tier2", 2, "false"),
						new ClientPreferences("739982664", "Retirement", "MIG", "Long", "Tier3", 3, "true")
				)
			);
		//Initializing the client service with 2 clients and their preferences
		service = new ClientService();
		service.saveNewClientDetails(clientList.get(0));
		service.saveNewClientDetails(clientList.get(1));
		service.addClientPreferences(clientPreferencesList.get(0));
		service.addClientPreferences(clientPreferencesList.get(1));
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
		assertEquals(service.getAllClientPreferencesList().size(), 2);
		service.addClientPreferences(clientPreferencesList.get(2));
		assertEquals(service.getAllClientPreferencesList().size(), 3);
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
		ClientPreferences clientPreference =  service.getClientPreference("1425922638");
		ClientPreferences expected = clientPreferencesList.get(0);
		assertEquals(clientPreference.equals(expected), true);
	}
	
	@Test
	void shouldHandleNullObjectForGetClientPreference() {
		assertThrows(NullPointerException.class, () -> {
			service.getClientPreference(null);
		});
	}

	@Test
	void shouldHandleInvalidIdForGetClientPreference() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getClientPreference("1236679496");
		});
	}
	
	//Updating Client Preferences
	@Test
	void shouldUpdateClientPreference() {
		String id = "1425922638";
		ClientPreferences clientPreference =  service.getClientPreference(id);
		assertEquals(clientPreference.getIncomeCategory(), "MIG");
		clientPreference.setIncomeCategory("HIG");
		service.updateClientPreferences(id, clientPreference);
		assertEquals(clientPreference.getIncomeCategory(), "HIG");
		assertEquals(service.getAllClientPreferencesList().size(), 2);
	}
	
	@Test
	void shouldNotUpdateForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			service.updateClientPreferences("1425922638", null);
		});
	}
	
	@Test
	void shouldNotUpdateForNullId() {
		assertThrows(NullPointerException.class, () -> {
			service.updateClientPreferences(null, clientPreferencesList.get(0));
		});
	}
	
	@Test
	void shouldNotUpdateForPreferenceThatDoesntExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.updateClientPreferences("1236679496", clientPreferencesList.get(2));
		});
	}
}
