package com.marshals.services;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.marshals.dao.ClientTradeDao;
import com.marshals.models.TradeHistory;
 
@Service("tradeHistoryService")
public class TradeHistoryService {

	 private ClientTradeDao dao;
	 
	 @Autowired
	 public TradeHistoryService(@Qualifier("clientTradeDao")ClientTradeDao dao) {
		 this.dao = dao;
	 }
	
	 public TradeHistory getClientTradeHistory(String clientId) {
        if (clientId == null) {
            throw new NullPointerException("Client ID must not be null");
        }
        TradeHistory tradeHistory;      
        tradeHistory = dao.getClientTradeHistory(clientId);
        return tradeHistory;
 
    }
 
	   
}