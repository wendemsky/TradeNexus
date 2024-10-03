package com.marshals.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.marshals.integration.mapper.ClientMapper;
import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;
import com.marshals.utils.EmailValidator;

@Repository("clientDao")
public class ClientDaoImpl implements ClientDao{
	
	@Autowired 
	private ClientMapper clientMapper;
	
	@Autowired
	private Logger logger;
	
	@Override
	public Boolean verifyClientEmail(String email) {
		logger.debug("In dao impl of verifying client email");
		try {
			//Check for valid email format
			if(!EmailValidator.isValidEmail(email))
				throw new IllegalArgumentException("Client Email Format is invalid");
			String clientEmail = clientMapper.verifyClientEmail(email);
			if(clientEmail == null) throw new DatabaseException("Client with given email doesnt exist");
		} catch(IllegalArgumentException e) {
			throw e;
		} catch(DatabaseException e) {
			logger.error("Error in verifying client email: "+e);
			throw e;
		}
		return true;
	}

	@Override
	public Client getClientAtLogin(String email, String password) {
		logger.debug("In dao impl of getting client info at login");
		Client client = null;
		try {
			client = clientMapper.getClientAtLogin(email); //Getting client details
			//If client details dont exist
			if(client==null) throw new DatabaseException("Logging in Client doesnt exist"); 
			if(!client.getPassword().equals(password)) //There is a mismatch in password information
				throw new IllegalArgumentException("Password does not match logging in Client's credentials");
		} catch(DatabaseException e) {
			logger.error("Error in verifying client email: "+e);
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		} 
		return client;
	}
	
	@Override
	public List<ClientIdentification> getAllClientIdentificationDetails() {
		logger.debug("In dao impl of getting client identification details for registration");
		List <ClientIdentification> identifications = new ArrayList<>();
		try {
			 identifications = clientMapper.getAllClientIdentificationDetails();
			 if(identifications ==null || identifications.size()<=0)  throw new DatabaseException("No Client Identification details to be retrieved");
			
		} catch(DatabaseException e) {
			logger.error("Error in getting all client identification details: "+e);
			throw e;
		} 
		return identifications;
	}

	@Override
	public void addNewClient(Client client, ClientPortfolio clientPortfolio) {
		logger.debug("In dao impl of saving new client details for registration");
		int rowsAffected = 0;
		try {
			rowsAffected = clientMapper.addNewClient(client,clientPortfolio);
			if(rowsAffected == 0) {
					throw new DatabaseException("Cannot insert client with ID "+client.getClientId());
			}
			//After inserting client - Insert each of the client identification details
			for(ClientIdentification identification : client.getIdentification()) {
				rowsAffected = clientMapper.addNewClientIdentificationDetails(client.getClientId(),identification);
				if(rowsAffected == 0) {
					throw new DatabaseException("Cannot insert identification details for client with ID "+client.getClientId());
				}
			}
				
		} catch(DuplicateKeyException e) {
			logger.error("Error in saving new client details - Should satisfy pkey requirements",e);
			throw new DatabaseException("Cannot insert client with ID "+client.getClientId());
		} 
		catch(DatabaseException e) {
			logger.error("Error in saving new client details: "+e);
			throw e;
		}
	}
	
	
	/*CLIENT PREFERENCES*/

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
