package com.fidelity.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

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
		final String queryToGetClientPreference = """
					SELECT cp.client_id, 
					    cp.investment_purpose,
					    cp.income_category,
					    cp.length_of_investment,
					    cp.percentage_of_spend,
					    cp.risk_tolerance,
					    cp.is_advisor_accepted
					FROM CLIENT_PREFERENCES cp
					WHERE cp.client_id = ?
				""";
		ClientPreferences clientPreference = null;
		try {
			Connection connection = dataSource.getConnection();
			int countOfRows = 0;
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToGetClientPreference)) {
				stmt.setString(1, clientId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					countOfRows++;
					String id = rs.getString("client_id");
					String investmentPurpose = rs.getString("investment_purpose");
					String incomeCategory = rs.getString("income_category");
					String lengthOfInvestment = rs.getString("length_of_investment");
					String percentageOfSpend = rs.getString("percentage_of_spend");
					int riskTolerance = rs.getInt("risk_tolerance");
					boolean isAdvisorAccepted = rs.getBoolean("is_advisor_accepted");
					clientPreference = new ClientPreferences(
							id, 
							investmentPurpose, 
							incomeCategory, 
							lengthOfInvestment, 
							percentageOfSpend, 
							riskTolerance, 
							isAdvisorAccepted
						);
				}
				if(countOfRows == 0) {
					throw new SQLException("Invalid Client ID");
				}
			} 
		}
		catch(SQLException e) {
			logger.error("Cannot complete get operation", e);
			throw new DatabaseException("Client ID does not exist", e);
		}
		return clientPreference;
	}

	@Override
	public void addClientPreferences(ClientPreferences clientPreferences) {
		final String queryToAddClientPreferences = """
					INSERT INTO client_preferences (
					    client_id,
					    investment_purpose,
					    income_category,
					    length_of_investment,
					    percentage_of_spend,
					    risk_tolerance,
					    is_advisor_accepted
					) VALUES (?, ?, ?, ?, ?, ?, ?)
				""";
		try {
			Connection connection = dataSource.getConnection();
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToAddClientPreferences)) {
				stmt.setString(1, clientPreferences.getClientId());
				stmt.setString(2, clientPreferences.getInvestmentPurpose());
				stmt.setString(3, clientPreferences.getIncomeCategory());
				stmt.setString(4, clientPreferences.getLengthOfInvestment());
				stmt.setString(5, clientPreferences.getPercentageOfSpend());
				stmt.setInt(6, clientPreferences.getRiskTolerance());
				stmt.setString(7, String.valueOf(clientPreferences.getAcceptAdvisor()));
				stmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("Cannot complete insert operation", e);
			if(e.getErrorCode() == 2291) {
				throw new DatabaseException("Cannot insert for Client that doenst exists", e);
			}
			else if(e.getErrorCode() == 1) {
				throw new DatabaseException("Cannot insert for Client ID that exists", e);
			}
		}
	}

	@Override
	public void updateClientPreferences(ClientPreferences clientPreferences) {
		final String queryToUpdateClientPreferences = """
					UPDATE client_preferences cp
					SET cp.investment_purpose = ?,
						cp.income_category = ?,
						cp.length_of_investment = ?,
						cp.percentage_of_spend = ?,
						cp.risk_tolerance = ?,
						cp.is_advisor_accepted = ?
					WHERE cp.client_id = ?
				""";
		int rowsAffected = 0;
		try {
			Connection connection = dataSource.getConnection();
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToUpdateClientPreferences)) {
				stmt.setString(1, clientPreferences.getInvestmentPurpose());
				stmt.setString(2, clientPreferences.getIncomeCategory());
				stmt.setString(3, clientPreferences.getLengthOfInvestment());
				stmt.setString(4, clientPreferences.getPercentageOfSpend());
				stmt.setInt(5, clientPreferences.getRiskTolerance());
				stmt.setString(6, String.valueOf(clientPreferences.getAcceptAdvisor()));
				stmt.setString(7, clientPreferences.getClientId());
				rowsAffected = stmt.executeUpdate();
				if(rowsAffected == 0) {
					throw new SQLException("Invalid Client ID");
				}
			}
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("Cannot complete insert operation", e);
			throw new DatabaseException("Client ID does not exist");
		}
	}

}
