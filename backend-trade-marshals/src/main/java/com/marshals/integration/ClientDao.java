package com.marshals.integration;

import java.util.List;

import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;

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
