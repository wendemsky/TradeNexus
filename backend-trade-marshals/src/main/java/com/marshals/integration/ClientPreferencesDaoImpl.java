package com.marshals.integration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.marshals.business.ClientPreferences;
import com.marshals.integration.mapper.ClientPreferencesMapper;

@Repository("clientPreferencesDao")
public class ClientPreferencesDaoImpl implements ClientPreferencesDao{

	@Autowired 
	private ClientPreferencesMapper clientMapper;
	
	@Autowired
	private Logger logger;

	@Override
	public ClientPreferences getClientPreferences(String clientId) {
		ClientPreferences preferences = clientMapper.getClientPreferences(clientId);
		if(preferences == null) {
			throw new DatabaseException("Client id does not exist in Database");
		}
		return preferences;
	}

	@Override
	public void addClientPreferences(ClientPreferences clientPreferences) {
		try {
			int rowsAffected = clientMapper.addClientPreferences(clientPreferences);
			if(rowsAffected == 0) {
				throw new DatabaseException("Client doesn't exist");
			}
		}
		catch(DataIntegrityViolationException e) {
			logger.error("Error inserting client preferences - Should satisfy integrity constraints",e);
			throw new DatabaseException("Error inserting client preferences - Should satisfy integrity constraints");
		}
		catch(DatabaseException e) {
			logger.error("Error in adding client preferences details: "+e);
			throw e;
		}
	}

	@Override
	public void updateClientPreferences(ClientPreferences clientPreferences) {
		try {
			int rowsAffected = clientMapper.updateClientPreferences(clientPreferences);
			if(rowsAffected == 0) {
				throw new DatabaseException("Client doesn't exist");
			}
		}
		catch(DataIntegrityViolationException e) {
			logger.error("Error updating client preferences - Should satisfy integrity constraints", e);
			throw new DatabaseException("Error updating client preferences - Should satisfy integrity constraints");
		}
		catch(DatabaseException e) {
			logger.error("Error in updating client preferences details: "+e);
			throw e;
		}
	}
}
