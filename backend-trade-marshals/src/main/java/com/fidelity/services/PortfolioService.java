package com.fidelity.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fidelity.integration.ClientTradeDao;
//Importing models
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;
import com.fidelity.models.Trade;


public class PortfolioService {

	private ClientTradeDao clientTradeDao;
	
	public PortfolioService(ClientTradeDao dao) {
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
	            // Handle the trade depending on the direction
	            if ("B".equals(executedTrade.getDirection())) {
	                // Buy
	                BigDecimal totalCostOfTrade = executedTrade.getExecutionPrice().multiply(new BigDecimal(executedTrade.getQuantity()));
	                if (clientPortfolio.getCurrBalance().compareTo(totalCostOfTrade) >= 0) {
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
	                } else {
	                    throw new IllegalArgumentException("Insufficient balance");
	                    // You can add additional error handling or logging here
	                }
	            } else if ("S".equals(executedTrade.getDirection())) {
	                // Sell
	                if (existingHolding.getQuantity() >= executedTrade.getQuantity()) {
	                    BigDecimal totalValueOfTrade = executedTrade.getExecutionPrice().multiply(new BigDecimal(executedTrade.getQuantity()));
	                    // Update balance
	                    clientPortfolio.setCurrBalance(clientPortfolio.getCurrBalance().add(totalValueOfTrade));
	                    clientTradeDao.updateClientBalance(clientPortfolio.getClientId(), clientPortfolio.getCurrBalance());
	                    
	                    // Update holding
	                    BigDecimal newAvgPrice = (existingHolding.getAvgPrice().multiply(new BigDecimal(existingHolding.getQuantity()))
	                            .subtract(totalValueOfTrade))
	                            .divide(new BigDecimal(existingHolding.getQuantity()).subtract(new BigDecimal(executedTrade.getQuantity())), BigDecimal.ROUND_HALF_UP);
	                    existingHolding.setAvgPrice(newAvgPrice);
	                    existingHolding.setQuantity(existingHolding.getQuantity()-(executedTrade.getQuantity()));
	
	                    clientTradeDao.updateClientHoldings(clientPortfolio.getClientId(), existingHolding);
		                
	                } else {
	                	 throw new IllegalArgumentException("Insufficient quantity to sell");
	                    // You can add additional error handling or logging here
	                }
	            }
	        } else {
	            // Holding doesn't exist, handle accordingly
	            if ("B".equals(executedTrade.getDirection())) {
	                // Buy
	                BigDecimal totalCostOfTrade = executedTrade.getExecutionPrice().multiply(new BigDecimal(executedTrade.getQuantity()));
	                if (clientPortfolio.getCurrBalance().compareTo(totalCostOfTrade) >= 0) {
	                    // Update balance
	                    clientPortfolio.setCurrBalance(clientPortfolio.getCurrBalance().subtract(totalCostOfTrade));
	                    clientTradeDao.updateClientBalance(clientPortfolio.getClientId(), clientPortfolio.getCurrBalance());
	                    
	                    // Create new holding
	                    Holding newHolding = new Holding(executedTrade.getInstrumentId(),executedTrade.getQuantity(), totalCostOfTrade.divide(new BigDecimal(executedTrade.getQuantity()), BigDecimal.ROUND_HALF_UP));
	                    clientTradeDao.addClientHoldings(clientPortfolio.getClientId(), newHolding);
	                } else {
	                	 throw new IllegalArgumentException("Insufficient balance");
	                    // You can add additional error handling or logging here
	                }
	            } else {
	            	 throw new IllegalArgumentException("Instrument not found for selling");
	                // You can add additional error handling or logging here
	            }
	        }
    
    	
    	}catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
     
    }
    
}
