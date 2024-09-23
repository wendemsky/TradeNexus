package com.fidelity.integration;

import java.util.List;

import com.fidelity.models.Client;
import com.fidelity.models.ClientIdentification;
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;

//Has functions related to Client related data access
public interface ClientDao {
	
	Boolean verifyClientEmail(String email); //Email Validation
	Client getClientAtLogin(String email, String password); //For login
	List<ClientIdentification> getAllClientIdentificationDetails(); //Required for ID validation of new client
	void addNewClient(Client client, ClientPortfolio clientPortfolio); //For register

	//Client Preferences
	ClientPreferences getClientPreferences(String clientId); 
	void addClientPreferences(ClientPreferences clientPreferences); 
	void updateClientPreferences(ClientPreferences clientPreferences);
}
