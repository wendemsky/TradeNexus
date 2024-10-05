package com.marshals.services;
 
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.marshals.dao.ClientActivityReportDao;
import com.marshals.models.Holding;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;
 
@Service("activityReportService")
public class ActivityReportService {
	
	private ClientActivityReportDao dao;
	
	private TradeHistoryService tradeHistoryService; 
	
	@Autowired
	public ActivityReportService(@Qualifier("clientActivityReportDao") ClientActivityReportDao activityDao, @Qualifier("tradeHistoryService") TradeHistoryService service) {
		//Initializing the reqd Daos and TradeHistory Service
		this.dao = activityDao;
		this.tradeHistoryService = service;
	}
	
	//Generate Report with Clients Holdings
	public List<Holding> generateHoldingsReport(String clientId) {
		if(clientId == null) {
			throw new NullPointerException("Client Id should not be null for Holdings");
		}
		return dao.getClientHoldings(clientId);
	}
	
	//Generate Report with Clients Trade History
	public TradeHistory generateTradeReport(String clientId) {
		//Return Trade History of given client's portfolio
		if(clientId == null) {
			throw new NullPointerException("Client Id should not be null for Trade History");
		}
		return tradeHistoryService.getClientTradeHistory(clientId);
	}
	
	//Generate Report with Clients P&L Data
	public Map<String, BigDecimal> generatePLReport(String clientId) {
		//Make use of Trade History
		  if(clientId == null) {
			 throw new NullPointerException("Client Id should not be null to calculate Profit Loss");
		  }
		  Map<String, BigDecimal> profitLossMap = new HashMap<>();
	      Map<String, BigDecimal> buyPositions = new HashMap<>();
	      TradeHistory tradeHistory = tradeHistoryService.getClientTradeHistory(clientId);
 
	      for (Trade trade : tradeHistory.getTrades()) {
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