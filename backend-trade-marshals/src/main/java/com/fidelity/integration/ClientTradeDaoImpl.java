package com.fidelity.integration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;
import com.fidelity.models.Holding;
import com.fidelity.models.Order;
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;

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
				    c.client_id,
				    c.curr_balance,
				    h.instrument_id,
				    h.quantity,
				    h.avg_price
				FROM
				    client c
				LEFT OUTER JOIN
				    holdings h ON c.client_id = h.client_id
				WHERE
				    c.client_id = ?
			""";
		ClientPortfolio clientPortfolio = null;
		try {
			Connection connection = dataSource.getConnection();
			try (PreparedStatement stmt = 
				connection.prepareStatement(queryToGetClientPortfolio)) {
				stmt.setString(1, clientId);
				ResultSet rs = stmt.executeQuery();
				String id = "";
				BigDecimal currBalance = new BigDecimal(0);
				List<Holding> holdings = new ArrayList<>();
				while (rs.next()) {
					id = rs.getString("client_id");
					currBalance = rs.getBigDecimal("curr_balance");
					if(rs.getString("instrument_id") == null) { //InstrumentId was null - Client has no holdings
						clientPortfolio = new ClientPortfolio(id,currBalance,holdings);
						break;
					}
					else {
						String instrumentId = rs.getString("instrument_id");
						int quantity = rs.getInt("quantity");
						BigDecimal avgPrice = rs.getBigDecimal("avg_price");
						holdings.add( new Holding( instrumentId, quantity, avgPrice ));
					}	
				}
				if(holdings.size()>0)
					clientPortfolio = new ClientPortfolio(id,  currBalance, holdings);
				if(clientPortfolio == null) {
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
	public void updateClientBalance(String clientId, BigDecimal currBalance) {
		final String queryToAddClientHolding = """
				UPDATE client 
				SET curr_balance = ? 
				WHERE client_id = ? 
				""";
		try {
			Connection connection = dataSource.getConnection();
			try(PreparedStatement stmt = connection.prepareStatement(queryToAddClientHolding)){
				stmt.setBigDecimal(1, currBalance);
				stmt.setString(2, clientId);
				int rowsUpdated = stmt.executeUpdate();
				if(rowsUpdated<=0) throw new SQLException("Client doesnt exist");
			}
		} catch(SQLException e) {
			logger.error("Cannot complete get operation", e);
			throw new DatabaseException("Client doesnt exist to update balance", e);
		}

		
	}
	
	@Override
	
	public void addClientHoldings(String clientId, Holding holding) {
		final String queryToAddClientHolding = """
				INSERT INTO holdings (client_id, instrument_id, quantity, avg_price) VALUES
					(?, ?, ?, ?)
				""";
		try {
			Connection connection = dataSource.getConnection();
			try(PreparedStatement stmt = connection.prepareStatement(queryToAddClientHolding)){
				stmt.setString(1, clientId);
				stmt.setString(2, holding.getInstrumentId());
				stmt.setInt(3,  holding.getQuantity());
				stmt.setBigDecimal(4, holding.getAvgPrice());
				stmt.executeUpdate();
			}
		} catch(SQLException e) {
			logger.error("Cannot complete get operation", e);
			throw new DatabaseException("Client does not exist", e);
		}
		
	}
	
	@Override
	public void updateClientHoldings(String clientId, Holding holding) {
		final String queryToAddClientHolding = """
				UPDATE holdings 
				SET quantity = ?, avg_price = ? 
				WHERE client_id = ? and instrument_id = ?
				""";
		try {
			Connection connection = dataSource.getConnection();
			try(PreparedStatement stmt = connection.prepareStatement(queryToAddClientHolding)){
				stmt.setInt(1,  holding.getQuantity());
				stmt.setBigDecimal(2, holding.getAvgPrice());
				stmt.setString(3, clientId);
				stmt.setString(4, holding.getInstrumentId());
				int rowsUpdated = stmt.executeUpdate();
				if(rowsUpdated<=0) throw new SQLException("Client Holdings dont exist to get updated");
			}
		} catch(SQLException e) {
			logger.error("Cannot complete get operation", e);
			throw new DatabaseException("Client Holdings dont exist to get updated", e);
		}
		
	}

	@Override
	public TradeHistory getClientTradeHistory(String clientId) {
		TradeHistory clientTradeHistory = null;
		List<Trade> fetchedTrades = new ArrayList<Trade>();
		final String queryToGetClientTradeHistoryById = """
				SELECT
				o.client_id, o.instrument_id, o.quantity, t.execution_price, o.direction, o.client_id, t.trade_id, t.cash_value, o.order_id, o.token, o.target_price
				FROM
				CLIENT_ORDER o
				INNER JOIN CLIENT_TRADE t
				ON 
				o.ORDER_ID = t.ORDER_ID
				WHERE
				o.client_id=?
				ORDER BY t.executed_at desc
								""";
		try {
			Connection conn = dataSource.getConnection();
			int countFetchedRows = 0;
			try(PreparedStatement stmt = conn.prepareStatement(queryToGetClientTradeHistoryById)){
				stmt.setString(1, clientId);
				ResultSet rs = stmt.executeQuery();
				while(rs.next()) {
					countFetchedRows++;
					String clientIdFetched = rs.getString("client_id");
					String instrumentId = rs.getString("instrument_id");
					int quantity = rs.getInt("quantity");
					BigDecimal executionPrice = rs.getBigDecimal("execution_price");
					String direction = rs.getString("direction");
					String tradeId = rs.getString("trade_id");
					BigDecimal cashValue = rs.getBigDecimal("cash_value");
//						Attributes to create an Order object
					String orderId = rs.getString("order_id");
					int token = rs.getInt("token");
					BigDecimal targetPrice = rs.getBigDecimal("target_price");
					Order clientOrder = new Order(instrumentId, quantity, targetPrice, direction, clientIdFetched, orderId, token);
					Trade clientTradeFetched = new Trade(clientOrder, executionPrice, tradeId, cashValue);
					fetchedTrades.add(clientTradeFetched);
					//Only get the last 100 trades of client
					if(countFetchedRows == 100)
						break;
				}
				if(countFetchedRows == 0) {
					throw new SQLException("Invalid Client ID");
				}
			}
		}catch(SQLException e) {
			logger.error("Error while executing get client trade history by client id query - {}", queryToGetClientTradeHistoryById, e);
			logger.error(e.getMessage());
			throw new DatabaseException();
		}
		clientTradeHistory = new TradeHistory(clientId, fetchedTrades);
		return clientTradeHistory;
	}
	
	@Override
	public void addTrade(Trade trade) {
		// TODO Auto-generated method stub
		final String queryToAddTradeToOrderTable = """
				INSERT INTO CLIENT_ORDER(instrument_id, quantity, target_price, direction, client_id, order_id, token) 
				VALUES(?, ?, ?, ?, ?, ?, ?)
				""";
		final String queryToAddTradeToTradeTable = """
				INSERT INTO CLIENT_TRADE(trade_id, order_id, execution_price, cash_value, executed_at) 
				VALUES(?, ?, ?, ?, ?)
				""";
		try {
			Connection conn = dataSource.getConnection();
			try(PreparedStatement stmt = conn.prepareStatement(queryToAddTradeToOrderTable)){
				stmt.setString(1, trade.getInstrumentId());
				stmt.setInt(2, trade.getQuantity());
				stmt.setBigDecimal(3, trade.getOrder().getTargetPrice());
				stmt.setString(4, trade.getDirection());
				stmt.setString(5, trade.getClientId());
				stmt.setString(6, trade.getOrder().getOrderId());
				stmt.setInt(7, trade.getOrder().getToken());
				stmt.executeUpdate();
			}
			try(PreparedStatement stmt = conn.prepareStatement(queryToAddTradeToTradeTable)){
				stmt.setString(1, trade.getTradeId());
				stmt.setString(2, trade.getOrder().getOrderId());
				stmt.setBigDecimal(3, trade.getExecutionPrice());
				stmt.setBigDecimal(4, trade.getCashValue());
				stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
				stmt.executeUpdate();
			}
		}catch(SQLException e) {
			logger.error(e.getMessage());
			throw new DatabaseException();
		}
	}

	

}
