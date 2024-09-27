package com.fidelity.services;

import java.util.Iterator;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

//Importing FMTS package
import com.fidelity.fmts.*;
import com.fidelity.integration.ClientDao;
import com.fidelity.integration.DatabaseException;
//Import models package
import com.fidelity.models.Client;
import com.fidelity.models.ClientIdentification;
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;
import com.fidelity.models.Holding;

//Importing Utils - Email validator
import com.fidelity.utils.EmailValidator;

public class ClientService {
	
	//Client Dao Object which interacts with DB
	private ClientDao clientDao;
	
	public ClientService(ClientDao dao) {
		this.clientDao = dao; //Intializing the Dao Object
	}
	
	/*Methods related to Client - Email Validation, Login and Register*/
	
	//Verifying Email Address - Checking if given client already exists
	public Boolean verifyClientEmail(String email) {
		try {
			if(email==null) 
				throw new NullPointerException("Client Email cannot be null");
			Boolean isClientExists = clientDao.verifyClientEmail(email);
			return isClientExists;
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) { //Email format is invalid
			throw e;
		} catch(DatabaseException e) { //Email not found
			return false;
		}
	}
	
	//Helper method - To verifying Client ID Details - If already exists or not - For registration
	private Boolean verifyClientIdentityDetails(ClientIdentification clientIdentification) {
		try {
			if(clientIdentification==null) 
				throw new NullPointerException("Client Identification Details cannot be null");
			 //Get the list of existing client identification details with dao
			 List<ClientIdentification> clientIdentifications = clientDao.getAllClientIdentificationDetails();
			Iterator<ClientIdentification> iter = clientIdentifications.iterator();
			for(ClientIdentification identification:clientIdentifications) {
				if(identification.equals(clientIdentification)) //If given clientIdentification exists
					return true;
			}
			return false;
		} catch(NullPointerException e) {
			throw e;
		}
	}

	//Registering a new client
	public Client registerNewClient(String email, String password, String name,
										String dateOfBirth, String country, List<ClientIdentification> identification) {
		try {
			Client client = null;
			//Verifying the email passed to check if client already exists or not
			if(verifyClientEmail(email))  //If email already registered
				throw new IllegalArgumentException("Client with given email is already registered");
			if(password==null || name==null || dateOfBirth==null || country==null)
				throw new NullPointerException("Client Details cannot be null");
			
			//Verifying the Client Identification information
			for(ClientIdentification id: identification) {
				if(verifyClientIdentityDetails(id)) //If identification details already present returns true
					throw new IllegalArgumentException("Client with given Identification Details is already registered with another email");
			}
			
			//Validating Client with FMTS
			ValidatedClient validatedClient = FMTSService.verifyClient(email); 
			if(validatedClient == null)
				throw new NullPointerException("New Client Details couldnt be verified");
			
			//ON SUCCESSFUL VERIFICATION OF REGISTRATION - Saving the clients details
			client = new Client(email,validatedClient.getClientId(), password, name, dateOfBirth, country, identification, false);
			saveNewClientDetails(client);
			return client;
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
	}
	
	//Save new Client Details
	private void saveNewClientDetails(Client newClient) {
		//Setting Client Portfolio details with initial Balance of 10k Dollars and empty holdings
		ClientPortfolio newClientPortfolio = new ClientPortfolio(newClient.getClientId(),new BigDecimal("10000").setScale(4),new ArrayList<Holding>());
		//Calling dao to add the new client details
		clientDao.addNewClient(newClient, newClientPortfolio);
	}
	
	//Logging in an existing client
	public Client loginExistingClient(String email, String password) {
		try {
			Client existingClient = null;
			
			//Verifying the email passed to check if client already exists or not
			if(!verifyClientEmail(email)) //If client not existing
				throw new IllegalArgumentException("Client with given email is not registered");
			if(password==null)
				throw new NullPointerException("Client Password cannot be null");
			
			//Calling dao to retrieve client details
			existingClient = clientDao.getClientAtLogin(email, password);
			
			//Validating Existing Client with FMTS
			ValidatedClient validatedClient = FMTSService.verifyClient(email,existingClient.getClientId()); 
			if(validatedClient == null)
				throw new NullPointerException("New Client Details couldnt be verified");
 
			//ON SUCCESSFUL LOGIN - Returning the client details
			return existingClient;
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
	}
		
	/*Methods Related to Adding and Updating of Client Preferences*/
	
	public void addClientPreferences(ClientPreferences preferences) {
		try {
			if(preferences == null) {
				throw new NullPointerException("preferences cannot be null");
			}
			clientDao.addClientPreferences(preferences);
		} catch(NullPointerException e) {
			throw e;
		}	
	}
	
	public ClientPreferences getClientPreference(String clientId) {
		try {
			if(clientId == null) {
				throw new NullPointerException("Id should not be null");
			}
			return clientDao.getClientPreferences(clientId);
		} catch(NullPointerException e) {
			throw e;
		}
	}
	
	public void updateClientPreferences(ClientPreferences preferences) {
		try {
			if(preferences == null) {
				throw new NullPointerException("Preferences should not be null");
			}
			clientDao.updateClientPreferences(preferences);
		} catch(NullPointerException e) {
			throw e;
		}
	}
		
}
