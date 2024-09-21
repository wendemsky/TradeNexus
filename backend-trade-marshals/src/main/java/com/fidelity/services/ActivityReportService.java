package com.fidelity.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;
import com.fidelity.models.Trade;

public class ActivityReportService {
	
	private static PortfolioService portfolioService = null;
	private static TradeHistoryService tradeHistoryService = null; 
	
	public ActivityReportService() {
		//Initializing Portfolio and Trade History Service
		portfolioService = new PortfolioService();
		tradeHistoryService = new TradeHistoryService();
	}
	
	//Generate Report with Clients Holdings
	public ClientPortfolio generateHoldingsReport(String clientId) {
		//Return Holdings of given client's portfolio
		return portfolioService.getClientPortfolio(clientId);
		
	}
	
	//Generate Report with Clients Trade History
	public List<Trade> generateTradeReport(String clientId) {
		//Return Trade History of given client's portfolio
		return tradeHistoryService.getClientTradeHistory(clientId);
	}
	
	//Generate Report with Clients P&L Data
	public static Map<String, BigDecimal> generatePLReport(String clientId) {
		//Make use of Trade History
		  Map<String, BigDecimal> profitLossMap = new HashMap<>();
	      Map<String, BigDecimal> buyPositions = new HashMap<>();
	      
	      List<Trade> trades = tradeHistoryService.getClientTradeHistory(clientId);

	      for (Trade trade : trades) {
	            if (!trade.getClientId().equals(clientId)) continue;

	            String instrumentId = trade.getInstrumentId();
	            BigDecimal tradeValue = trade.getCashValue();
	            
	            if (trade.getDirection().equals("B")) {
	                // Use BigDecimal's add method
	                buyPositions.put(instrumentId, tradeValue.add(buyPositions.getOrDefault(instrumentId, BigDecimal.ZERO)));
	            } else if (trade.getDirection().equals("S")) {
	                // Get the buyValue as BigDecimal
	                BigDecimal buyValue = buyPositions.getOrDefault(instrumentId, BigDecimal.ZERO);
	                
	                // Calculate profit/loss using BigDecimal's subtract method
	                BigDecimal profitLoss = buyValue.subtract(tradeValue);
	                profitLossMap.put(instrumentId, profitLossMap.getOrDefault(instrumentId, BigDecimal.ZERO).add(profitLoss));
	                
	                // After selling, we assume the position is cleared
	                buyPositions.remove(instrumentId);
	            }
	        }
	        return profitLossMap;
	    }
	}
	

