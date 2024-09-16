package com.fidelity.clientportfolio;

import java.util.ArrayList;
import java.util.List;



public class TradeHistoryService {
	 private List<Trade> trades = new ArrayList<>();
	 public List<Trade> getClientTradeHistory(String clientId) {
	        if (clientId == null) {
	            throw new NullPointerException("Client ID must not be null");
	        }

	        List<Trade> result = new ArrayList<>();
	        for (Trade trade : trades) {
	            if (trade.getClientId().equals(clientId)) {
	                result.add(trade);
	            }
	        }

	        if (result.isEmpty()) {
	            throw new RuntimeException("No trades found for client ID");
	        }

	        return result;
	    }

	    // Method to add a trade
	    public void addTrade(Trade trade) {
	        if (trade == null) {
	            throw new NullPointerException("Trade must not be null");
	        }
	        trades.add(trade);
	    }

	    // Method to update a trade - placeholder implementation
	    public void updateTrade(Trade updatedTrade) {
	        if (updatedTrade == null) {
	            throw new NullPointerException("Updated trade must not be null");
	        }

	        boolean found = false;
	        for (int i = 0; i < trades.size(); i++) {
	            if (trades.get(i).getTradeId().equals(updatedTrade.getTradeId())) {
	                trades.set(i, updatedTrade);
	                found = true;
	                break;
	            }
	        }

	        if (!found) {
	            throw new RuntimeException("Trade not found for ID: " + updatedTrade.getTradeId());
	        }
	    }


}
