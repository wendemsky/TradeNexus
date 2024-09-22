package com.fidelity.integration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;
import com.fidelity.models.Holding;
import com.fidelity.models.Trade;

public class ClientTradeDaoImpl implements ClientTradeDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public ClientTradeDaoImpl(DataSource ds) {
		dataSource = ds;
	}

	@Override
	public ClientPortfolio getClientPortfolio(String clientId) {
		final String queryToGetClientPortfolio = """
				SELECT 
				    cp.client_id,
				    cp.curr_balance,
				    h.instrument_id,
				    h.quantity,
				    h.avg_price
				FROM
				    client_portfolio cp
				LEFT JOIN
				    holdings h ON cp.client_id = h.client_id
				WHERE
				    cp.client_id = ?;
			""";
		ClientPortfolio clientPortfolio = null;
		try {
			Connection connection = dataSource.getConnection();
			int countOfRows = 0;
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToGetClientPortfolio)) {
				stmt.setString(1, clientId);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					countOfRows++;
					String id = rs.getString("client_id");
					BigDecimal currBalance = rs.getBigDecimal("curr_balance");
					String instrumentId = rs.getString("instrument_id");
					int quantity = rs.getInt("quantity");
					BigDecimal avgPrice = rs.getBigDecimal("avg_price");
					
					List<Holding> holdings = new ArrayList<Holding>();
					holdings = List.of( new Holding( instrumentId, quantity,avgPrice ));
					
					clientPortfolio = new ClientPortfolio(
							id, 
							currBalance,
							holdings
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
		return clientPortfolio;
	}

	@Override
	public void addClientPortfolio(ClientPortfolio clientPortfolio) {
		// TODO Auto-generated method stub
		final String queryToAddClientPortfolio = """
				INSERT INTO client_portfolio (client_id, curr_balance)
				) VALUES (?, ?)
			""";
		final String queryToAddHoldings = """
				INSERT INTO holdings (client_id, instrument_id, quantity, avg_price)
				) VALUES (?, ?, ?, ?)
			""";
		
	try {
		Connection connection = dataSource.getConnection();
		try (PreparedStatement stmt = 
			connection.prepareStatement(queryToAddClientPortfolio)) {
			stmt.setString(1, clientPortfolio.getClientId());
			stmt.setBigDecimal(2, clientPortfolio.getCurrBalance());
			stmt.executeUpdate();
		}
		try (PreparedStatement holdingStmt = 
                connection.prepareStatement(queryToAddHoldings)) {
            for (Holding holding : clientPortfolio.getHoldings()) {
                holdingStmt.setString(1, clientPortfolio.getClientId());
                holdingStmt.setString(2, holding.getInstrumentId());
                holdingStmt.setInt(3, holding.getQuantity());
                holdingStmt.setBigDecimal(4, holding.getAvgPrice());
                holdingStmt.executeUpdate();
            }
        }
	}
	catch (SQLException e) {
		// TODO Auto-generated catch block
		logger.error("Cannot complete insert operation", e);
			throw new DatabaseException("Cannot insert for Client portfolio");
	}
		
}

	@Override
	public void updateClientPortfolio(ClientPortfolio clientPortfolio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getClientTradeHistory(String clientId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTrade(Trade trade) {
		// TODO Auto-generated method stub
		
	}

}
