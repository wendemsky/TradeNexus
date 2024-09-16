package com.fidelity.activityreport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fidelity.clientportfolio.ClientPortfolio;
import com.fidelity.clientportfolio.PortfolioService;
import com.fidelity.clientportfolio.TradeHistoryService;
import com.fidelity.trade.Trade;
import com.fidelity.trade.TradeService;

public class ActivityReportService {
	
	public PortfolioService portfolioService = new PortfolioService();
	public TradeHistoryService tradeHistoryService = new TradeHistoryService();
	
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
//	public static void generatePLReport(String clientId) {
//		//Make use of Trade History
//		  Map<String, Double> profitLossMap = new HashMap<>();
//	      Map<String, Double> buyPositions = new HashMap<>();
//
//	      for (Trade trade : trades) {
//	            if (!trade.getClientId().equals(clientId)) continue;
//
//	            String instrumentId = trade.getInstrumentId();
//	            double tradeValue = trade.getQuantity() * trade.getExecutionPrice();
//	            
//	            if (trade.getDirection().equals("buy")) {
//	                buyPositions.put(instrumentId, buyPositions.getOrDefault(instrumentId, 0.0) + tradeValue);
//	            } else if (trade.getDirection().equals("sell")) {
//	                double buyValue = buyPositions.getOrDefault(instrumentId, 0.0);
//	                double profitLoss = buyValue - tradeValue;
//	                profitLossMap.put(instrumentId, profitLossMap.getOrDefault(instrumentId, 0.0) + profitLoss);
//	                
//	                // After selling, we assume the position is cleared
//	                buyPositions.remove(instrumentId);
//	            }
//	        }
//	        
//	        return profitLossMap;
//	    }
	}
	

