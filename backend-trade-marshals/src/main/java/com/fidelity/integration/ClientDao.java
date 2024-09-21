package com.fidelity.integration;

import com.fidelity.models.Client;
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;

//Has functions related to Client related data access
public interface ClientDao {
	
	Boolean verifyClientEmail(String email);
	Client getClientAtLogin(String email); //For login
	void addNewClient(Client client, ClientPortfolio clientPortfolio); //For register

	//Client Preferences
	ClientPreferences getClientPreferences(String clientId); 
	void addClientPreferences(ClientPreferences clientPreferences); 
	void updateClientPreferences(ClientPreferences clientPreferences);
}
