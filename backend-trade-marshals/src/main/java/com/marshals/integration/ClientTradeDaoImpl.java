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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
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

	@Autowired
	private Logger logger;

 
	@Override
	public ClientPortfolio getClientPortfolio(String clientId) {
		 ClientPortfolio clientPortfolio = clientTradeMapper.getClientPortfolio(clientId);
        if (clientPortfolio == null) {
            throw new DatabaseException("Client ID does not exist");
        }
        return clientPortfolio;
	}
 
	@Override
	public void updateClientBalance(String clientId, BigDecimal currBalance) {
		int rowcount=0; 
		try {
	            rowcount = clientTradeMapper.updateClientBalance(clientId, currBalance);
	            if(rowcount ==0) {
	            	 throw new DatabaseException("Client does not exist to update balance");
	            }
	        } catch (Exception e) {
	            throw e;
	        }
	}
	@Override
	public TradeHistory getClientTradeHistory(String clientId) {
		List<Trade> trades = new ArrayList<>();
		try {
			logger.debug("enter");
			trades = clientTradeMapper.getClientTradeHistory(clientId);
			System.out.println("Trades when invalid clientId is passed -> " + trades + ", clientId -> " + clientId);
			if(trades.isEmpty()) {
				throw new DatabaseException("Client ID does not exist");
			}
			TradeHistory tradeHistory = new TradeHistory(clientId, trades);
			return tradeHistory;
		}catch(DatabaseException e) {
			logger.error("Error while executing get client trade history by client id ", e);
			logger.error(e.getMessage());
			throw e;
		}
	}
	public void addClientHoldings(String clientId, Holding holding) {
		int rowcount = 0;
		try {
            rowcount = clientTradeMapper.addClientHoldings(clientId, holding);
            if(rowcount == 0) {
           	 throw new DatabaseException("Client does not exist to add holding");
           }
       } catch (DatabaseException e) {
           throw e;
       } catch (DataIntegrityViolationException e) {
    	   throw new DatabaseException("Client does not exist to add holding");
       }
	}
	@Transactional
	@Override
	public void updateClientHoldings(String clientId, Holding holding) {
		int rowcount =0;
		try {
	            rowcount = clientTradeMapper.updateClientHoldings(clientId, holding);
 
		 	if(rowcount ==0) {
           	 throw new DatabaseException("Client does not exist to update holdings");
           }
       }  catch (DatabaseException e) {
           throw e;
       } catch (DataIntegrityViolationException e) {
    	   throw new DatabaseException("Client does not exist to update holding");
       }
	}
 
	@Override
	public void addTrade(Trade trade) {
		int orderRowcount = 0;
		int tradeRowcount = 0;
		try {
			logger.debug("enter");
			orderRowcount = clientTradeMapper.addOrder(trade.getOrder());
			tradeRowcount = clientTradeMapper.addTrade(trade);
			if(orderRowcount == 0) {
				throw new DatabaseException("couldn't add order to order table");
			}
			if(tradeRowcount == 0) {
				throw new DatabaseException("couldn't add trade to trade table");
			}
		} catch(DataAccessException e) {
		 	logger.error(e.getMessage());
			throw new DatabaseException(e.getMessage());
		} catch(DatabaseException e) {
			logger.error(e.getMessage());
			throw new DatabaseException(e.getMessage());
		}
	}
 
}