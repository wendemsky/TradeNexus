package com.fidelity.services;
 
import java.util.ArrayList;
import java.util.List;
 
//Importing models
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;
 
public class TradeHistoryService {
	 private List<TradeHistory> tradeHistory;
	 public TradeHistoryService() {
		 this.tradeHistory = new ArrayList<>();
	 }
	 public void addTrade(Trade trade) {
		 if (trade == null) {
	         throw new NullPointerException("Trade must not be null");
	     }
		 String clientId = trade.getClientId();
	        if (clientId == null) {
	            throw new NullPointerException("Trade must have a valid client ID");
	    }
	    for (TradeHistory th : tradeHistory) {
	         if (th.getClientId().equals(clientId)) {
	             th.getTrades().add(trade);
	             return;
	         }
	    }
 
	    List<Trade> trades = new ArrayList<>();
	    trades.add(trade);
	    TradeHistory newTradeHistory = new TradeHistory(clientId, trades);
	    tradeHistory.add(newTradeHistory);
	 }
	 public List<Trade> getClientTradeHistory(String clientId) {
	        if (clientId == null) {
	            throw new NullPointerException("Client ID must not be null");
	        }
 
	        for (TradeHistory tradeHistory : tradeHistory) {
	        	 if (tradeHistory.getClientId().equals(clientId)) {
	                 return new ArrayList<>(tradeHistory.getTrades());
	             }
	        }
	        throw new RuntimeException("No trades found for client ID");
 
	    }
 
	    // Method to update a trade - placeholder implementation
	    public void updateTrade(Trade updatedTrade) {
	        if (updatedTrade == null) {
	            throw new NullPointerException("Updated trade must not be null");
	        }
 
	        boolean found = false;
	        for (TradeHistory th : tradeHistory) {
	            if (th.getClientId().equals(updatedTrade.getClientId())) {
	                List<Trade> trades = th.getTrades();
	                for (int i = 0; i < trades.size(); i++) {
	                    if (trades.get(i).getTradeId().equals(updatedTrade.getTradeId())) {
	                        trades.set(i, updatedTrade);
	                        found = true;
	                        break;
	                    }
	                }
	                break; 
	            }
	        }
 
 
	        if (!found) {
	            throw new RuntimeException("Trade not found for ID: " + updatedTrade.getTradeId());
	        }
	    }
}