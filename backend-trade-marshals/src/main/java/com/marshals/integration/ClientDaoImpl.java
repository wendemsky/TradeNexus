package com.marshals.integration;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;
import com.marshals.utils.EmailValidator;

public class ClientDaoImpl implements ClientDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy"); //doB Format stored in client model
	
	public ClientDaoImpl(DataSource ds) {
		dataSource = ds;
	}

	@Override
	public Boolean verifyClientEmail(String email) {
		final String sql = """
				SELECT email
				FROM client 
				WHERE email= ?
				""";
		try {
			//Check for valid email format
			if(!EmailValidator.isValidEmail(email))
				throw new IllegalArgumentException("Client Email Format is invalid");
			Connection connection = dataSource.getConnection();
			try(PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, email);
				ResultSet rs = stmt.executeQuery();
				//If client details could not be retrieved
				if(!rs.next()) throw new DatabaseException("Client with given email doesnt exist");
			}

		} catch(IllegalArgumentException e) {
			throw e;
		} catch (SQLException e) {
			logger.error("There was an error in retrieving client with given email {}",e);	
			throw new DatabaseException("Client with given email couldnt be retrieved");
		} catch(DatabaseException e) {
			throw e;
		}
		return true;
	}

	@Override
	public Client getClientAtLogin(String email, String password) {
		final String sql = """
				SELECT c.client_id, c.email, c.password, c.name, c.date_of_birth, c.country, c.is_admin, ci.type, ci.value
				FROM client c
				INNER JOIN client_identification ci
				ON c.client_id = ci.client_id
				WHERE c.email = ?
				ORDER BY c.client_id
			""";
		Client client = null;
		try {
			Connection connection = dataSource.getConnection();
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, email);
				ResultSet rs = stmt.executeQuery();
				//Client details
				String client_id = "";
				String name = "";
				String dateOfBirth = "";
				String country  = "";
				Boolean isAdmin = false;
				List <ClientIdentification> identification = new ArrayList<>();
				//Going through the result set
				while (rs.next()) { //Logging in client details are found
					client_id = rs.getString("client_id");
					String db_password = rs.getString("password");
					if(!db_password.equals(password)) //There is a mismatch in password information
						throw new IllegalArgumentException("Password does not match logging in Client's credentials");
					name = rs.getString("name");
					//Change your date format to string
					java.sql.Date dbDate = rs.getDate("date_of_birth");
			        dateOfBirth = dateFormat.format(dbDate);
			        country = rs.getString("country");
			        isAdmin = "Y".equals(rs.getString("is_admin"));
			        //Identification details is a list
			        identification.add(new ClientIdentification(rs.getString("type"),rs.getString("value"))); 
				}
				if(identification.size()>0) //When details are retrieved
					client = new Client(email,client_id, password, name, dateOfBirth, country, 
						identification, isAdmin);
				//If client details dont exist
				if(client==null) throw new DatabaseException("Logging in Client doesnt exist");
			} 
		}
		catch(SQLException e) {
			logger.error("There was an error in retrieving client details",e);	
			throw new DatabaseException("Logging in client details couldnt be retrieved");
		} catch(DatabaseException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
		return client;
	}
	
	@Override
	public List<ClientIdentification> getAllClientIdentificationDetails() {
		final String sql = """
				SELECT type, value
				FROM client_identification
				ORDER BY client_id
			""";
		//Client identification details
		List <ClientIdentification> identifications = new ArrayList<>();
		try {
			Connection connection = dataSource.getConnection();
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				ResultSet rs = stmt.executeQuery();		
				//Going through the result set
				while (rs.next()) { //Logging in client details are found
			        identifications.add(new ClientIdentification(rs.getString("type"),rs.getString("value"))); 
				}	
				if(identifications.size()<=0)  throw new DatabaseException("No Client Identification details to be retrieved");
			} 
		}
		catch(SQLException e) {
			logger.error("There was an error in retrieving client identification details",e);	
			throw new DatabaseException("Client Identification couldnt be retrieved");
		} catch(DatabaseException e) {
			throw e;
		} 
		return identifications;
	}

	@Override
	public void addNewClient(Client client, ClientPortfolio clientPortfolio) {
		//Insert into client
		String sql = """
				INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try {
			Connection conn = dataSource.getConnection();
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				//Setting values	
				stmt.setString(1, client.getClientId());
				stmt.setString(2, client.getEmail());
				stmt.setString(3, client.getPassword());
				stmt.setString(4, client.getName());
				//Setting of date of birth
				java.util.Date parsedDate;
				try {
					parsedDate = dateFormat.parse(client.getDateOfBirth());
				} catch (ParseException e) { //If there is an error, assign it will date 18 years back (Min age)
					parsedDate = java.sql.Date.valueOf(LocalDate.now().minusYears(18)); 
				}
	            java.sql.Date dateOfBirthSql = new Date(parsedDate.getTime());
				stmt.setDate(5, dateOfBirthSql);
				stmt.setString(6, client.getCountry());
				//Setting of isAdmin
				String isAdminString = client.getIsAdmin() ? "Y" : "N";
				stmt.setString(7, isAdminString);
				//Setting of currBalance retrieved from ClientPortfolio
				stmt.setBigDecimal(8, clientPortfolio.getCurrBalance());
				//Execute update
				stmt.executeUpdate();
				//After inserting client - Insert each of the client identification details
				for(ClientIdentification identification : client.getIdentification()) {
					addNewClientIdentification(client.getClientId(),identification);
				}
			}	
		} catch (SQLException e) {
			logger.error("Cannot insert client {}", sql, e);
			throw new DatabaseException("Cannot insert client with ID " +client.getClientId());
		} catch(DatabaseException e) {
			throw e;
		}
	}

	//Helper function to insert client identification
	private void addNewClientIdentification(String clientId, ClientIdentification identification) {
		//Insert into client identification
		String sql = """
				INSERT INTO client_identification (client_id, type, value) 
				VALUES (?, ?, ?)
				""";
		try {
			Connection conn = dataSource.getConnection();
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				//Setting values	
				stmt.setString(1, clientId);
				stmt.setString(2, identification.getType());
				stmt.setString(3, identification.getValue());
				//Execute update
				stmt.executeUpdate();
			}	
		} catch (SQLException e) {
			logger.error("Cannot insert client identification of type {} {} {}", identification.getType(), sql, e);
			throw new DatabaseException("Cannot insert client identification of type "+ identification.getType()+" with ID "+ clientId);
		} catch(DatabaseException e) {
			throw e;
		}
	}
	
	
	/*CLIENT PREFERENCES*/

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
			if(e.getErrorCode() == 2291) { //Fkey error
				throw new DatabaseException("Cannot insert preferences for Client that doenst exist", e);
			}
			else if(e.getErrorCode() == 1) {
				throw new DatabaseException("Cannot insert preferences for Client with existing preferences", e);
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
			logger.error("Cannot complete update operation", e);
			throw new DatabaseException("Cannot update preferences for Client that doenst exist");
		}
	}

	

}
