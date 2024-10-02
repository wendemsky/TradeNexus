package com.marshals.integration;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.marshals.integration.mapper.ClientTradeMapper;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;
import com.marshals.models.Holding;
import com.marshals.models.Order;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;

@Repository("clientTradeDao")
public class ClientTradeDaoImpl implements ClientTradeDao {
	
	@Autowired
	 private ClientTradeMapper clientTradeMapper;
	
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public ClientTradeDaoImpl(DataSource ds) {
		dataSource = ds;
	}


	public ClientPortfolio getClientPortfolio(String clientId) {
		 ClientPortfolio clientPortfolio = clientTradeMapper.getClientPortfolio(clientId);
	        if (clientPortfolio == null) {
	            throw new DatabaseException("Client ID does not exist");
	        }
	        return clientPortfolio;
	}

	public void updateClientBalance(String clientId, BigDecimal currBalance) {
		 try {
	            clientTradeMapper.updateClientBalance(clientId, currBalance);
	        } catch (Exception e) {
	            logger.error("Cannot complete update operation", e);
	            throw new DatabaseException("Client does not exist to update balance", e);
	        }
	}
	
	public void addClientHoldings(String clientId, Holding holding) {
		try {
            clientTradeMapper.updateClientHoldings(clientId, holding);
        } catch (Exception e) {
            logger.error("Cannot complete insert operation", e);
            throw new DatabaseException("Client does not exist", e);
        }
	}
	
	public void updateClientHoldings(String clientId, Holding holding) {
		 try {
	            clientTradeMapper.updateClientHoldings(clientId, holding);
	        } catch (Exception e) {
	            logger.error("Cannot complete update operation", e);
	            throw new DatabaseException("Client holdings do not exist to get updated", e);
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
