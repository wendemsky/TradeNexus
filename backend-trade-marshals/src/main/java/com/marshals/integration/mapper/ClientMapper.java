package com.marshals.integration.mapper;

import java.util.List;

import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;

public interface ClientMapper {

	//Client Preferences
	ClientPreferences getClientPreferences(String clientId); 
	int addClientPreferences(ClientPreferences clientPreferences); //returns number of rows affected
	int updateClientPreferences(ClientPreferences clientPreferences);//returns number of rows updated
	
}