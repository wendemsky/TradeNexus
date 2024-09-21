package com.fidelity.integration;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.Client;
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;

public class ClientDaoImpl implements ClientDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public ClientDaoImpl(DataSource ds) {
		dataSource = ds;
	}

	@Override
	public Boolean verifyClientEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Client getClientAtLogin(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNewClient(Client client, ClientPortfolio clientPortfolio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClientPreferences getClientPreferences(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addClientPreferences(ClientPreferences clientPreferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateClientPreferences(ClientPreferences clientPreferences) {
		// TODO Auto-generated method stub
		
	}

}
