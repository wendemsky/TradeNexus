package com.fidelity.client;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ClientService {

	private List<ClientPreferences> clientPreferences;
	
	public ClientService() {
		this.clientPreferences = new ArrayList<ClientPreferences>();
	}
	
	public void addClientPreferences(ClientPreferences preferences) {
		// DB Logic
		if(preferences == null) {
			throw new NullPointerException("preferences cannot be null");
		}
		clientPreferences.add(preferences);
	}
	
	public ClientPreferences getClientPreference(String clientId) {
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
	}
	
	public void updateClientPreferences(String clientId, ClientPreferences preferences) {
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
	}
	
	public List<ClientPreferences> getAllPreferenceList() {
		return this.clientPreferences;
	}
}
