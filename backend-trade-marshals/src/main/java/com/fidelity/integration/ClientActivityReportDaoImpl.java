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
import com.fidelity.models.Holding;

public class ClientActivityReportDaoImpl implements ClientActivityReportDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public ClientActivityReportDaoImpl (DataSource ds) {
		dataSource = ds;
	}

	@Override
	public List<Holding> getClientHoldings(String clientId) {
		List<Holding> holdings = new ArrayList<>();
		final String queryToGetClientPortfolio = """
				SELECT 
				    instrument_id,
				    quantity,
				    avg_price
				FROM
				    holdings
				WHERE
				    client_id = ?
			""";
		try {
			Connection connection = dataSource.getConnection();
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToGetClientPortfolio)) {
				stmt.setString(1, clientId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String instrumentId = rs.getString("instrument_id");
					int quantity = rs.getInt("quantity");
					BigDecimal avgPrice = rs.getBigDecimal("avg_price");
					holdings.add( new Holding( instrumentId, quantity, avgPrice ));
				}
				if(holdings.size()<=0) {
					throw new SQLException("Client has no holdings");
				}
			} 
		}
		catch(SQLException e) {
			logger.error("Cannot complete get operation", e);
			throw new DatabaseException(e.getMessage());
		}
		return holdings;
	}

}
