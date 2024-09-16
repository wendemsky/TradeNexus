package com.fidelity.activityreport;

public class ActivityReportService {
	
	//Generate Report with Clients Holdings
	public static void generateHoldingsReport(String clientId) {
		//Return Holdings of given client's portfolio
	}
	
	//Generate Report with Clients Trade History
	public static void generateTradeReport(String clientId) {
		//Return Trade History of given client's portfolio
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
//	}
	
}
