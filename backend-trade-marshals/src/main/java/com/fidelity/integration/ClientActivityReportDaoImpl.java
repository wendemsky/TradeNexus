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
				    cp.client_id,
				    h.instrument_id,
				    h.quantity,
				    h.avg_price
				FROM
				    client_portfolio cp
				LEFT JOIN
				    holdings h ON cp.client_id = h.client_id
				WHERE
				    cp.client_id = ?
			""";
		try {
			Connection connection = dataSource.getConnection();
			int countOfRows = 0;
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToGetClientPortfolio)) {
				stmt.setString(1, clientId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					countOfRows++;
					String instrumentId = rs.getString("instrument_id");
					int quantity = rs.getInt("quantity");
					BigDecimal avgPrice = rs.getBigDecimal("avg_price");
					holdings.add( new Holding( instrumentId, quantity, avgPrice ));
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
		return holdings;
	}

}
