package com.marshals.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.marshals.integration.ClientTradeDao;
import com.marshals.integration.DatabaseException;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.Holding;
import com.marshals.models.Trade;

@Service("portfolioService")
public class PortfolioService {

	private ClientTradeDao clientTradeDao;
	
	@Autowired
	public PortfolioService(@Qualifier("clientTradeDao") ClientTradeDao dao) {
		this.clientTradeDao = dao;
	}

    public ClientPortfolio getClientPortfolio(String clientId) {
    	try {
			if(clientId == null) {
				throw new NullPointerException("Id should not be null");
			}
			return clientTradeDao.getClientPortfolio(clientId);
		} catch(NullPointerException e) {
			throw e;
		}
    }

    public void updateClientPortfolio(Trade executedTrade) {
    	try {
			if(executedTrade == null) {
				throw new NullPointerException("Trade should not be null");
			}
			//Get client's portfolio
			ClientPortfolio clientPortfolio = getClientPortfolio(executedTrade.getClientId());
			//Get client's holdings and check if exec trade already exists
			List<Holding> clientHoldings = clientPortfolio.getHoldings();
			Holding existingHolding = null;
			for (Holding holding : clientHoldings) {
	            if (holding.getInstrumentId().equals(executedTrade.getInstrumentId())) {
	                existingHolding = holding;
	                break;
	            }
	        }
			if (existingHolding != null) {
	            // If holding exists - update holding
	            if ("B".equals(executedTrade.getDirection())) { //Buy
	                // Calculating total cost of trade
	                BigDecimal totalCostOfTrade = executedTrade.getCashValue();  
                    
	                // Update balance
                    clientPortfolio.setCurrBalance(clientPortfolio.getCurrBalance().subtract(totalCostOfTrade));
                    clientTradeDao.updateClientBalance(clientPortfolio.getClientId(), clientPortfolio.getCurrBalance());
                    
                    // Update holding
                    BigDecimal newAvgPrice = (existingHolding.getAvgPrice().multiply(new BigDecimal(existingHolding.getQuantity()))
                            .add(totalCostOfTrade))
                            .divide(new BigDecimal(existingHolding.getQuantity()).add(new BigDecimal(executedTrade.getQuantity())), BigDecimal.ROUND_HALF_UP);
                    existingHolding.setAvgPrice(newAvgPrice);
                    existingHolding.setQuantity(existingHolding.getQuantity()+ executedTrade.getQuantity());
                    clientTradeDao.updateClientHoldings(clientPortfolio.getClientId(), existingHolding);
	                
	            } else if ("S".equals(executedTrade.getDirection())) {  // Sell	
	                          
                    BigDecimal totalValueOfTrade = executedTrade.getCashValue();
                    
                    // Update balance
                    clientPortfolio.setCurrBalance(clientPortfolio.getCurrBalance().add(totalValueOfTrade));
                    clientTradeDao.updateClientBalance(clientPortfolio.getClientId(), clientPortfolio.getCurrBalance());
                    
                    // Update holding
                    BigDecimal newAvgPrice = (existingHolding.getAvgPrice().multiply(new BigDecimal(existingHolding.getQuantity()))
                            .subtract(totalValueOfTrade))
                            .divide(new BigDecimal(existingHolding.getQuantity()).subtract(new BigDecimal(executedTrade.getQuantity())), BigDecimal.ROUND_HALF_UP);
                    System.out.println("New Avg Price "+newAvgPrice);
                    existingHolding.setAvgPrice(newAvgPrice);
                    existingHolding.setQuantity(existingHolding.getQuantity()-(executedTrade.getQuantity()));

                    clientTradeDao.updateClientHoldings(clientPortfolio.getClientId(), existingHolding);             
	            }
	        } else {
	            // Holding doesn't exist, add holding
	            if ("B".equals(executedTrade.getDirection())) { //Buy
	             
	                BigDecimal totalCostOfTrade = executedTrade.getCashValue();
	                    // Update balance
	                    clientPortfolio.setCurrBalance(clientPortfolio.getCurrBalance().subtract(totalCostOfTrade));
	                    clientTradeDao.updateClientBalance(clientPortfolio.getClientId(), clientPortfolio.getCurrBalance());
	                    
	                    // Create new holding
	                    Holding newHolding = new Holding(executedTrade.getInstrumentId(),executedTrade.getQuantity(), totalCostOfTrade.divide(new BigDecimal(executedTrade.getQuantity()), BigDecimal.ROUND_HALF_UP));
	                    clientTradeDao.addClientHoldings(clientPortfolio.getClientId(), newHolding);
	            } else { //Cannot sell if holding doesnt exist
	            	 throw new IllegalArgumentException("Instrument not part of holdings! Cannot sell and update the portfolio");
	            }
	        }
    
    	
    	}catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
     
    }
    
}
