package com.fidelity.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
	public Boolean verifyClientEmail(String email) {
		String sql = """
				SELECT email
				FROM client 
				WHERE email= ?
				""";
		try {
			Connection connection = dataSource.getConnection();
			try(PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, email);
				ResultSet rs = stmt.executeQuery();
				//If client details could not be retrieved
				if(!rs.next()) throw new DatabaseException("Client with given email doesnt exist");
			}

		} catch (SQLException e) {
			logger.error("There was an error in retrieving client with given email {}",e);	
			throw new DatabaseException("Client with given email couldnt be retrieved");
		} catch(DatabaseException e) {
			throw e;
		}
		return true;
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
