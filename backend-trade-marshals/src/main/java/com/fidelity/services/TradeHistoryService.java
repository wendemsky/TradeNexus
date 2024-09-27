package com.fidelity.services;
 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.fidelity.integration.ClientTradeDao;
import com.fidelity.integration.ClientTradeDaoImpl;
import com.fidelity.integration.DatabaseException;
//Importing models
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;
 
public class TradeHistoryService {

	 private ClientTradeDao dao;
	 
	 public TradeHistoryService(ClientTradeDao dao) {
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