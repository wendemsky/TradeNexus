package com.marshals.dao;

import com.marshals.models.ClientPreferences;

//Has functions related to Client preferences related data access
public interface ClientPreferencesDao {

	ClientPreferences getClientPreferences(String clientId);

	void addClientPreferences(ClientPreferences clientPreferences);

	void updateClientPreferences(ClientPreferences clientPreferences);
}
