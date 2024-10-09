package com.marshals.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.marshals.integration.ClientPreferencesDao;
import com.marshals.models.ClientPreferences;

@Service("clientPreferencesService")
public class ClientPreferencesService {

	// Client Dao Object which interacts with DB
	private ClientPreferencesDao clientPreferencesDao;

	@Autowired
	public ClientPreferencesService(@Qualifier("clientPreferencesDao") ClientPreferencesDao dao) {
		this.clientPreferencesDao = dao; //Intializing the Dao Object
	}
	
	/* Methods Related to Adding and Updating of Client Preferences */
	public void addClientPreferences(ClientPreferences preferences) {
		try {
			if (preferences == null) {
				throw new NullPointerException("preferences cannot be null");
			}
			clientPreferencesDao.addClientPreferences(preferences);
		} catch (NullPointerException e) {
			throw e;
		}
	}

	public ClientPreferences getClientPreferences(String clientId) {
		try {
			if (clientId == null) {
				throw new NullPointerException("Id should not be null");
			}
			return clientPreferencesDao.getClientPreferences(clientId);
		} catch (NullPointerException e) {
			throw e;
		}
	}

	public void updateClientPreferences(ClientPreferences preferences) {
		try {
			if (preferences == null) {
				throw new NullPointerException("Preferences should not be null");
			}
			clientPreferencesDao.updateClientPreferences(preferences);
		} catch (NullPointerException e) {
			throw e;
		}
	}
}
