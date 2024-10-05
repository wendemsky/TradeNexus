package com.marshals.integration.mapper;

import com.marshals.models.ClientPreferences;

public interface ClientPreferencesMapper {

	// Client Preferences
	ClientPreferences getClientPreferences(String clientId);
	int addClientPreferences(ClientPreferences clientPreferences); // returns number of rows affected
	int updateClientPreferences(ClientPreferences clientPreferences);// returns number of rows updated
}
