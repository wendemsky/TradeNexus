package com.fidelity.clientportfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fidelity.trade.*;


public class PortfolioService {
	
	private List<ClientPortfolio> clientPortfolios;
	
	public PortfolioService() {
		this.clientPortfolios = new ArrayList<ClientPortfolio>();
	}
	
    
    public ClientPortfolio addClientPortfolio(ClientPortfolio clientPortfolio) {
     try {
	   	 if (clientPortfolio == null) {
	            throw new NullPointerException("Client portfolio must not be null");
	      }
	   	 clientPortfolios.add(clientPortfolio);
	   	 return clientPortfolio;
     } catch(NullPointerException e) {
    	 throw e;
     }
    
   }

    public ClientPortfolio getClientPortfolio(String clientId) {
    	try {
			if(clientId == null) {
				throw new NullPointerException("Client ID should not be null");
			}
			Iterator<ClientPortfolio> iter = clientPortfolios.iterator();
			while(iter.hasNext()) {
				ClientPortfolio portfolio = iter.next();
				if(portfolio.getClientId() == clientId) {
					return portfolio;
				}
			}
			throw new IllegalArgumentException("Client Portfolio is not existing");	
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
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
	 
	                    // Update holding
	                    BigDecimal newAvgPrice = (existingHolding.getAvgPrice().multiply(new BigDecimal(existingHolding.getQuantity()))
	                            .add(totalCostOfTrade))
	                            .divide(new BigDecimal(existingHolding.getQuantity()).add(new BigDecimal(executedTrade.getQuantity())), BigDecimal.ROUND_HALF_UP);
	                    existingHolding.setAvgPrice(newAvgPrice);
	                    existingHolding.setQuantity(existingHolding.getQuantity()+ executedTrade.getQuantity());
	                } else {
	                    System.err.println("Insufficient balance");
	                    // You can add additional error handling or logging here
	                }
	            } else if ("S".equals(executedTrade.getDirection())) {
	                // Sell
	                if (existingHolding.getQuantity() >= executedTrade.getQuantity()) {
	                    BigDecimal totalValueOfTrade = executedTrade.getExecutionPrice().multiply(new BigDecimal(executedTrade.getQuantity()));
	                    // Update balance
	                    clientPortfolio.setCurrBalance(clientPortfolio.getCurrBalance().add(totalValueOfTrade));
	 
	                    // Update holding
	                    BigDecimal newAvgPrice = (existingHolding.getAvgPrice().multiply(new BigDecimal(existingHolding.getQuantity()))
	                            .subtract(totalValueOfTrade))
	                            .divide(new BigDecimal(existingHolding.getQuantity()).subtract(new BigDecimal(executedTrade.getQuantity())), BigDecimal.ROUND_HALF_UP);
	                    existingHolding.setAvgPrice(newAvgPrice);
	                    existingHolding.setQuantity(existingHolding.getQuantity()-(executedTrade.getQuantity()));
	 
	                    // Remove holding if quantity becomes 0
	                    if (existingHolding.getQuantity() == 0) {
	                        clientHoldings.remove(existingHolding);
	                    }
	                } else {
	                    System.err.println("Insufficient quantity to sell");
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
	 
	                    // Create new holding
	                    Holding newHolding = new Holding(executedTrade.getInstrumentId(),executedTrade.getQuantity(), totalCostOfTrade.divide(new BigDecimal(executedTrade.getQuantity()), BigDecimal.ROUND_HALF_UP));
	                    // Add new holding to the list
	                    clientHoldings.add(newHolding);
	                } else {
	                    System.err.println("Insufficient balance");
	                    // You can add additional error handling or logging here
	                }
	            } else {
	                System.err.println("Instrument not found for selling");
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
