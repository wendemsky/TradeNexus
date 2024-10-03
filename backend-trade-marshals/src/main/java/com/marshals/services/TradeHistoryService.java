package com.marshals.services;
 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.marshals.integration.ClientTradeDao;
import com.marshals.integration.ClientTradeDaoImpl;
import com.marshals.integration.DatabaseException;
import com.marshals.models.Trade;
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