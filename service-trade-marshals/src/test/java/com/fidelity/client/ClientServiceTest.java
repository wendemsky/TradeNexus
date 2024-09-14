package com.fidelity.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientServiceTest {
	
	private ClientService service;
	private List<ClientPreferences> preferenceList;

	@BeforeEach
	void setUp() throws Exception {
		preferenceList = new ArrayList<ClientPreferences>(
				List.of(
						new ClientPreferences("1425922638", "Education", "MIG", "Short", "Tier1", 5, "true"),
						new ClientPreferences("1654658069",  "Major Expense", "LIG", "Medium", "Tier2", 2, "false"),
						new ClientPreferences("1236679496", "Retirement", "MIG", "Long", "Tier3", 3, "true")
				)
			);
		service = new ClientService();
		service.addClientPreferences(preferenceList.get(0));
		service.addClientPreferences(preferenceList.get(1));
	}

	@AfterEach
	void tearDown() throws Exception {
		service = null;
		preferenceList = null;
	}

	@Test
	void shouldCreateObject() {
		assertNotNull(service);
	}
	
	@Test
	void shouldAddClientPreference() {
		assertEquals(service.getAllPreferenceList().size(), 2);
		service.addClientPreferences(preferenceList.get(2));
		assertEquals(service.getAllPreferenceList().size(), 3);
	}
	
	@Test
	void shouldHandleNullObjectForAddClientPreference() {
		assertThrows(NullPointerException.class, () -> {
			service.addClientPreferences(null);
		});
	}
	
	@Test
	void shouldGetClientPreference() {
		ClientPreferences clientPreference =  service.getClientPreference("1425922638");
		ClientPreferences expected = preferenceList.get(0);
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
	
	@Test
	void shouldUpdateClientPreference() {
		String id = "1425922638";
		ClientPreferences clientPreference =  service.getClientPreference(id);
		assertEquals(clientPreference.getIncomeCategory(), "MIG");
		clientPreference.setIncomeCategory("HIG");
		service.updateClientPreferences(id, clientPreference);
		assertEquals(clientPreference.getIncomeCategory(), "HIG");
		assertEquals(service.getAllPreferenceList().size(), 2);
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
			service.updateClientPreferences(null, preferenceList.get(0));
		});
	}
	
	@Test
	void shouldNotUpdateForPreferenceThatDoesntExist() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.updateClientPreferences("1236679496", preferenceList.get(2));
		});
	}
}
