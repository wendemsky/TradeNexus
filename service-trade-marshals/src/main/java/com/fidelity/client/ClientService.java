package com.fidelity.client;

import java.util.Iterator;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

//Importing FMTS package
import com.fidelity.fmts.*;
//Import Client Portfolio package
import com.fidelity.clientportfolio.*;

public class ClientService {

	private List<Client> clients;
	private List<ClientPreferences> clientPreferences;
	
	public ClientService() {
		this.clients = new ArrayList<Client>();
		this.clientPreferences = new ArrayList<ClientPreferences>();
	}
	
	/*Methods related to Client - Email Validation, Login and Register*/
	
	//Verifying Email Address - Checking if given client already exists
	public Client verifyClientEmail(String email) {
		try {
			if(email==null) 
				throw new NullPointerException("Client Email cannot be null");
			Iterator<Client> iter = clients.iterator();
			while(iter.hasNext()) {
				Client client = iter.next();
				if(client.getEmail() == email) { //If client found - Return true
					return client;
				}
			}
			return null; //If not found
		} catch(NullPointerException e) {
			throw e;
		}
	}
	
	//Verifying Client ID Details - If already exists or not
	public Boolean verifyClientIdentityDetails(ClientIdentification clientIdentification) {
		try {
			if(clientIdentification==null) 
				throw new NullPointerException("Client Identification Details cannot be null");

			Iterator<Client> iter = clients.iterator();
			while(iter.hasNext()) {
				Client existingClient = iter.next();
				for(ClientIdentification id: existingClient.getIdentificationDetails()) { //Iterating through each client's ID details
					if(id.equals(clientIdentification))  //If client ID details found - Return true
						return true;
				}	
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
			
			//Verifying the email passed
			client = verifyClientEmail(email); //If email already registered returns existing client
			if(client!=null) 
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
	public void saveNewClientDetails(Client newClient) {
		clients.add(newClient);
		//Saving Client Portfolio details with initial Balance of 100k Dollars and empty holdings
		ClientPortfolio newClientPortfolio = new ClientPortfolio(newClient.getClientId(),new BigDecimal("100000").setScale(4),new ArrayList<Holding>());
		PortfolioService service = new PortfolioService();
		service.addClientPortfolio(newClientPortfolio);
	}
	
	//Logging in an existing client
	public Client loginExistingClient(String email, String password) {
		try {
			Client existingClient = null;
			//Verifying the email passed
			existingClient = verifyClientEmail(email);
			if(existingClient==null) //If client not existing
				throw new IllegalArgumentException("Client with given email is not registered");
			
			if(password==null)
				throw new NullPointerException("Client Password cannot be null");
			//Verify if the password matches given client
			if(existingClient.getPassword() != password)
				throw new IllegalArgumentException("Password does not match given Client's credentials");
			
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
	
	public List<ClientPreferences> getAllClientPreferencesList() {
		return this.clientPreferences;
	}
	
	public void addClientPreferences(ClientPreferences preferences) {
		try {
			if(preferences == null) {
				throw new NullPointerException("preferences cannot be null");
			}
			clientPreferences.add(preferences);
		} catch(NullPointerException e) {
			throw e;
		}	
	}
	
	public ClientPreferences getClientPreference(String clientId) {
		try {
			if(clientId == null) {
				throw new NullPointerException("Id should not be null");
			}
			Iterator<ClientPreferences> iter = clientPreferences.iterator();
			while(iter.hasNext()) {
				ClientPreferences member = iter.next();
				if(member.getClientId() == clientId) {
					return member;
				}
			}
			throw new IllegalArgumentException("Invalid ID");	
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
			
	}
	
	public void updateClientPreferences(String clientId, ClientPreferences preferences) {
		try {
			if(clientId == null) {
				throw new NullPointerException("Id should not be null");
			}
			if(preferences == null) {
				throw new NullPointerException("Id should not be null");
			}
			Iterator<ClientPreferences> iter = clientPreferences.iterator();
			while(iter.hasNext()) {
				ClientPreferences member = iter.next();
				if(member.getClientId() == clientId) {
					member = preferences;
					return;
				}
			}
			throw new IllegalArgumentException("No user with this ID exists");	
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
	}
		

}
